package com.example.sonicflow.presentation.screen.playlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sonicflow.domain.model.Playlist
import kotlinx.coroutines.delay

private val GRADIENT_COLORS = listOf(
    Color(0xFF6366F1),
    Color(0xFF8B5CF6),
    Color(0xFFEC4899),
    Color(0xFFF97316)
)

private val PLAYLIST_CARD_GRADIENT = listOf(
    Color(0xFF2D1B4E),
    Color(0xFF1F1535)
)

@Composable
fun PlaylistContent(
    viewModel: PlaylistViewModel,
    onPlaylistClick: (Long) -> Unit,
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit
) {
    val playlists by viewModel.playlists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    // Filtrer les playlists selon la recherche
    val filteredPlaylists = remember(playlists, searchQuery) {
        if (searchQuery.isEmpty()) {
            playlists
        } else {
            playlists.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            delay(100)
            focusRequester.requestFocus()
        } else {
            searchQuery = ""
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (!isLoading && playlists.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = GRADIENT_COLORS[2],
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Créer une playlist",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                                onRetry = { viewModel.loadPlaylists() }
                            )
                        }
                        filteredPlaylists.isEmpty() && searchQuery.isNotEmpty() -> {
                            ModernEmptySearchState(searchQuery = searchQuery)
                        }
                        playlists.isEmpty() -> {
                            ModernEmptyPlaylistState(
                                onCreateClick = { showCreateDialog = true }
                            )
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    horizontal = 16.dp,
                                    vertical = 12.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (searchQuery.isNotEmpty()) {
                                    item {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color.White.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = "${filteredPlaylists.size} playlist${if (filteredPlaylists.size > 1) "s" else ""}",
                                                modifier = Modifier.padding(16.dp),
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }

                                items(filteredPlaylists, key = { it.id }) { playlist ->
                                    ModernPlaylistCard(
                                        playlist = playlist,
                                        onClick = { onPlaylistClick(playlist.id) }
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
        }

        // Dialog: Créer nouvelle playlist
        if (showCreateDialog) {
            ModernCreatePlaylistDialog(
                playlistName = newPlaylistName,
                onNameChange = { newPlaylistName = it },
                onDismiss = {
                    showCreateDialog = false
                    newPlaylistName = ""
                },
                onConfirm = {
                    viewModel.createPlaylist(newPlaylistName)
                    showCreateDialog = false
                    newPlaylistName = ""
                }
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
                        "Rechercher des playlists...",
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
                "Chargement des playlists...",
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
                color = GRADIENT_COLORS[2].copy(alpha = 0.15f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = GRADIENT_COLORS[2],
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
                color = GRADIENT_COLORS[0].copy(alpha = 0.15f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = GRADIENT_COLORS[0],
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
                "Aucune playlist trouvée pour \"$searchQuery\"",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ModernEmptyPlaylistState(
    onCreateClick: () -> Unit
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
            val infiniteTransition = rememberInfiniteTransition(label = "empty_playlist")
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
                color = GRADIENT_COLORS[1].copy(alpha = 0.15f),
                modifier = Modifier
                    .size(96.dp)
                    .scale(scale)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.PlaylistPlay,
                        contentDescription = null,
                        tint = GRADIENT_COLORS[1],
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Text(
                "Aucune playlist",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Text(
                "Créez votre première playlist\npour organiser votre musique.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onCreateClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GRADIENT_COLORS[1]
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Créer une playlist",
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
private fun ModernPlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit
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
                        colors = PLAYLIST_CARD_GRADIENT
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icône playlist
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GRADIENT_COLORS[playlist.id.toInt() % GRADIENT_COLORS.size].copy(alpha = 0.2f),
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.PlaylistPlay,
                            contentDescription = null,
                            tint = GRADIENT_COLORS[playlist.id.toInt() % GRADIENT_COLORS.size],
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Info playlist
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${playlist.trackCount} morceau${if (playlist.trackCount > 1) "x" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                // Flèche
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Voir la playlist",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
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
                color = GRADIENT_COLORS[1].copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.PlaylistAdd,
                        contentDescription = null,
                        tint = GRADIENT_COLORS[1],
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
                        focusedBorderColor = GRADIENT_COLORS[1],
                        focusedLabelColor = GRADIENT_COLORS[1],
                        cursorColor = GRADIENT_COLORS[1],
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
                    containerColor = GRADIENT_COLORS[1],
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