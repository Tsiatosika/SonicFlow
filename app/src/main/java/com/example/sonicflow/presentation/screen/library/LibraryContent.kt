package com.example.sonicflow.presentation.screen.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.presentation.components.AlbumArtwork
import kotlinx.coroutines.delay
import kotlin.math.sin

// 🎨 PALETTE MODERNE - GRADIENTS DYNAMIQUES (cohérent avec HomeScreen)
private val GRADIENT_COLORS = listOf(
    Color(0xFF6366F1),  // Indigo
    Color(0xFF8B5CF6),  // Violet
    Color(0xFFEC4899),  // Rose
    Color(0xFFF97316)   // Orange
)

private val ACCENT_COLORS = listOf(
    Color(0xFF06B6D4),  // Cyan
    Color(0xFFEC4899),  // Rose
    Color(0xFF8B5CF6)   // Violet
)

// 🎵 Couleur des cartes de morceaux - Gradient sombre
private val TRACK_CARD_GRADIENT = listOf(
    Color(0xFF2D1B4E),  // Violet foncé
    Color(0xFF1F1535)   // Violet très foncé
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryContent(
    viewModel: LibraryViewModel,
    onTrackClick: (Track) -> Unit,
    onAlbumClick: (albumName: String, artistName: String) -> Unit,
    onArtistClick: (artistName: String) -> Unit,
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
        viewModel.loadTracks()  // ✅ Charger les tracks maintenant (avec permission)
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // SearchBar moderne
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                ModernSearchBar(
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
                        ModernLoadingState()
                    }
                    error != null -> {
                        ModernErrorState(
                            error = error!!,
                            onRetry = { viewModel.loadTracks() }
                        )
                    }
                    tracks.isEmpty() && searchQuery.isNotEmpty() -> {
                        ModernEmptySearchState(searchQuery = searchQuery)
                    }
                    tracks.isEmpty() -> {
                        ModernEmptyLibraryState(
                            onScan = { viewModel.refreshTracks() }
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                horizontal = 16.dp,
                                vertical = 12.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = tracks,
                                key = { track -> track.id }
                            ) { track ->
                                ModernTrackItem(
                                    track = track,
                                    isPlaying = currentPlayingTrack?.id == track.id,
                                    onClick = { onTrackClick(track) },
                                    onLongClick = {
                                        selectedTrack = track
                                        showTrackMenu = true
                                    },
                                    onMenuClick = {
                                        selectedTrack = track
                                        showTrackMenu = true
                                    }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }
        }

        // Track Menu Bottom Sheet
        if (showTrackMenu && selectedTrack != null) {
            ModernTrackMenuSheet(
                track = selectedTrack!!,
                onDismiss = {
                    showTrackMenu = false
                    selectedTrack = null
                },
                onAddToPlaylist = {
                    showPlaylistDialog = true
                    showTrackMenu = false
                },
                onViewAlbum = {
                    selectedTrack?.let { track ->
                        onAlbumClick(track.album, track.artist)
                    }
                    showTrackMenu = false
                },
                onViewArtist = {
                    selectedTrack?.let { track ->
                        onArtistClick(track.artist)
                    }
                    showTrackMenu = false
                },
                onToggleFavorite = {
                    selectedTrack?.let { track ->
                        viewModel.toggleFavorite(track)
                    }
                },
                onShowDetails = {
                    selectedTrack?.let { track ->
                        viewModel.showTrackDetails(track)
                    }
                    showTrackMenu = false
                },
                isFavorite = selectedTrack?.let { track ->
                    viewModel.favoriteTracks.value.contains(track.id)
                } ?: false
            )
        }

        // Playlist Selection Dialog
        if (showPlaylistDialog && selectedTrack != null) {
            ModernPlaylistDialog(
                playlists = playlists,
                onDismiss = {
                    showPlaylistDialog = false
                    selectedTrack = null
                },
                onPlaylistSelected = { playlist ->
                    selectedTrack?.let { track ->
                        viewModel.addTrackToPlaylist(playlist.id, track.id)
                    }
                    showPlaylistDialog = false
                    selectedTrack = null
                },
                onCreateNew = {
                    showPlaylistDialog = false
                    showCreatePlaylistDialog = true
                }
            )
        }

        // Create Playlist Dialog
        if (showCreatePlaylistDialog && selectedTrack != null) {
            ModernCreatePlaylistDialog(
                playlistName = newPlaylistName,
                onNameChange = { newPlaylistName = it },
                onDismiss = {
                    showCreatePlaylistDialog = false
                    newPlaylistName = ""
                    selectedTrack = null
                },
                onConfirm = {
                    if (newPlaylistName.isNotBlank()) {
                        selectedTrack?.let { track ->
                            viewModel.createPlaylistAndAddTrack(newPlaylistName, track.id)
                        }
                        showCreatePlaylistDialog = false
                        newPlaylistName = ""
                        selectedTrack = null
                    }
                }
            )
        }

        // Track Details Dialog
        if (showTrackDetailsDialog) {
            ModernTrackDetailsDialog(
                trackDetails = trackDetailsText,
                onDismiss = { viewModel.dismissTrackDetails() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    focusRequester: FocusRequester
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.1f),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )

            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        "Rechercher...",
                        color = Color.White.copy(alpha = 0.5f)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                singleLine = true
            )

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") }
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Effacer",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            IconButton(
                onClick = onClose
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Fermer",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }
}

@Composable
private fun ModernLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = GRADIENT_COLORS[1],
                modifier = Modifier.size(48.dp)
            )
            Text(
                "Chargement...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ModernErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = ACCENT_COLORS[1].copy(alpha = 0.15f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = ACCENT_COLORS[1],
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                "Erreur",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Text(
                error,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GRADIENT_COLORS[1]
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Réessayer")
            }
        }
    }
}

@Composable
private fun ModernEmptySearchState(searchQuery: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = ACCENT_COLORS[0].copy(alpha = 0.15f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = ACCENT_COLORS[0],
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                "Aucun résultat",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Text(
                "Aucun morceau trouvé pour \"$searchQuery\"",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ModernEmptyLibraryState(
    onScan: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Icône animée
            val infiniteTransition = rememberInfiniteTransition(label = "empty_state")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale_animation"
            )

            Surface(
                shape = CircleShape,
                color = GRADIENT_COLORS[2].copy(alpha = 0.15f),
                modifier = Modifier
                    .size(96.dp)
                    .scale(scale)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = GRADIENT_COLORS[2],
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Text(
                "Bibliothèque vide",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Text(
                "Aucun morceau trouvé sur cet appareil.\nAjoutez de la musique pour commencer.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onScan,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GRADIENT_COLORS[2]
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Scanner la bibliothèque",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTrackMenuSheet(
    track: Track,
    onDismiss: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onViewAlbum: () -> Unit,
    onViewArtist: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShowDetails: () -> Unit,
    isFavorite: Boolean
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // En-tête du morceau
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GRADIENT_COLORS[1].copy(alpha = 0.2f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = GRADIENT_COLORS[1],
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Divider(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Options du menu
            ModernMenuItem(
                icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                text = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                onClick = {
                    onToggleFavorite()
                    onDismiss()
                },
                iconTint = if (isFavorite) ACCENT_COLORS[1] else Color.White
            )

            ModernMenuItem(
                icon = Icons.Default.PlaylistAdd,
                text = "Ajouter à une playlist",
                onClick = onAddToPlaylist
            )

            ModernMenuItem(
                icon = Icons.Default.Album,
                text = "Voir l'album",
                onClick = onViewAlbum
            )

            ModernMenuItem(
                icon = Icons.Default.Person,
                text = "Voir l'artiste",
                onClick = onViewArtist
            )

            ModernMenuItem(
                icon = Icons.Default.Info,
                text = "Détails du morceau",
                onClick = onShowDetails
            )
        }
    }
}

@Composable
private fun ModernMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    iconTint: Color = Color.White
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ModernTrackDetailsDialog(
    trackDetails: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color(0xFF1A1A1A),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        icon = {
            Surface(
                shape = CircleShape,
                color = GRADIENT_COLORS[0].copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = GRADIENT_COLORS[0],
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        title = {
            Text(
                "Détails du morceau",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Text(
                trackDetails,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = GRADIENT_COLORS[0]
                )
            ) {
                Text("Fermer")
            }
        }
    )
}

@Composable
private fun ModernPlaylistDialog(
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Playlist) -> Unit,
    onCreateNew: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color(0xFF1A1A1A),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        icon = {
            Surface(
                shape = CircleShape,
                color = GRADIENT_COLORS[2].copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.PlaylistAdd,
                        contentDescription = null,
                        tint = GRADIENT_COLORS[2],
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        title = {
            Text(
                "Ajouter à une playlist",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (playlists.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Aucune playlist disponible",
                            color = Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    playlists.take(5).forEach { playlist ->
                        ModernPlaylistOption(
                            playlist = playlist,
                            onClick = { onPlaylistSelected(playlist) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onCreateNew,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GRADIENT_COLORS[2]
                    ),
                    shape = RoundedCornerShape(12.dp),
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
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                Text("Annuler")
            }
        }
    )
}

@Composable
private fun ModernPlaylistOption(
    playlist: Playlist,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.05f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = GRADIENT_COLORS[playlist.id.toInt() % GRADIENT_COLORS.size].copy(alpha = 0.2f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.PlaylistPlay,
                        contentDescription = null,
                        tint = GRADIENT_COLORS[playlist.id.toInt() % GRADIENT_COLORS.size],
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${playlist.trackCount} morceau${if (playlist.trackCount > 1) "x" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun ModernCreatePlaylistDialog(
    playlistName: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color(0xFF1A1A1A),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        icon = {
            Surface(
                shape = CircleShape,
                color = GRADIENT_COLORS[2].copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.PlaylistAdd,
                        contentDescription = null,
                        tint = GRADIENT_COLORS[2],
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        title = {
            Text(
                "Nouvelle playlist",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column {
                Text(
                    "Donnez un nom à votre playlist",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = onNameChange,
                    placeholder = {
                        Text(
                            "Ma playlist",
                            color = Color.White.copy(alpha = 0.3f)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GRADIENT_COLORS[2],
                        focusedLabelColor = GRADIENT_COLORS[2],
                        cursorColor = GRADIENT_COLORS[2],
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF2A2A2A),
                        unfocusedContainerColor = Color(0xFF2A2A2A)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = playlistName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GRADIENT_COLORS[2],
                    contentColor = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = 0.1f),
                    disabledContentColor = Color.White.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Créer",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Annuler")
            }
        }
    )
}

// 🎵 COMPOSANT CARTE DE MORCEAU MODERNE AVEC GRADIENT
@Composable
private fun ModernTrackItem(
    track: Track,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = TRACK_CARD_GRADIENT
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ✅ Icône de musique avec image d'album
                AlbumArtwork(
                    albumArtUri = track.albumArtUri,
                    size = 52.dp,
                    cornerRadius = 12.dp,
                    iconSize = 28.dp,
                    gradientColors = if (isPlaying)
                        listOf(
                            GRADIENT_COLORS[1].copy(alpha = 0.6f),
                            GRADIENT_COLORS[1].copy(alpha = 0.3f)
                        )
                    else
                        listOf(
                            Color.White.copy(alpha = 0.2f),
                            Color.White.copy(alpha = 0.1f)
                        )
                )

                // Infos du morceau
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${track.artist} • ${track.album}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Durée
                Text(
                    text = formatDuration(track.duration),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f)
                )

                // Menu
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// Fonction utilitaire pour formater la durée
private fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000).toInt()
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", minutes, secs)
}