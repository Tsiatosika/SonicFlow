package com.example.sonicflow.presentation.screen.album

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.sonicflow.domain.model.Album
import com.example.sonicflow.presentation.components.AlbumArtwork
import kotlinx.coroutines.delay

private val GRADIENT_COLORS = listOf(
    Color(0xFF6366F1),
    Color(0xFF8B5CF6),
    Color(0xFFEC4899),
    Color(0xFFF97316)
)

private val ALBUM_CARD_GRADIENT = listOf(
    Color(0xFF2D1B4E),
    Color(0xFF1F1535)
)

@Composable
fun AlbumContent(
    viewModel: AlbumViewModel,
    onAlbumClick: (Album) -> Unit,
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit
) {
    val albums by viewModel.albums.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    var showSortMenu by remember { mutableStateOf(false) }

    // Filtrer les albums selon la recherche
    val filteredAlbums = remember(albums, searchQuery) {
        if (searchQuery.isEmpty()) {
            albums
        } else {
            albums.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true)
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

    Box(modifier = Modifier.fillMaxSize()) {
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

            // Barre de tri moderne
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.05f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredAlbums.size} album${if (filteredAlbums.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Surface(
                        onClick = { showSortMenu = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Sort,
                                contentDescription = "Trier",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                getSortOrderLabel(sortOrder),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    // Menu de tri
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(
                            color = Color(0xFF1A1A1A),
                            shape = RoundedCornerShape(16.dp)
                        )
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text("Nom d'album", color = Color.White)
                            },
                            onClick = {
                                viewModel.setSortOrder(AlbumViewModel.SortOrder.ALBUM_NAME)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (sortOrder == AlbumViewModel.SortOrder.ALBUM_NAME) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = GRADIENT_COLORS[1])
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text("Artiste", color = Color.White)
                            },
                            onClick = {
                                viewModel.setSortOrder(AlbumViewModel.SortOrder.ARTIST_NAME)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (sortOrder == AlbumViewModel.SortOrder.ARTIST_NAME) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = GRADIENT_COLORS[1])
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text("Année (récent)", color = Color.White)
                            },
                            onClick = {
                                viewModel.setSortOrder(AlbumViewModel.SortOrder.YEAR_DESC)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (sortOrder == AlbumViewModel.SortOrder.YEAR_DESC) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = GRADIENT_COLORS[1])
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text("Année (ancien)", color = Color.White)
                            },
                            onClick = {
                                viewModel.setSortOrder(AlbumViewModel.SortOrder.YEAR_ASC)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (sortOrder == AlbumViewModel.SortOrder.YEAR_ASC) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = GRADIENT_COLORS[1])
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text("Nombre de morceaux", color = Color.White)
                            },
                            onClick = {
                                viewModel.setSortOrder(AlbumViewModel.SortOrder.TRACK_COUNT)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (sortOrder == AlbumViewModel.SortOrder.TRACK_COUNT) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = GRADIENT_COLORS[1])
                                }
                            }
                        )
                    }
                }
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
                            error = error ?: "Erreur inconnue",
                            onRetry = { viewModel.loadAlbums() }
                        )
                    }
                    filteredAlbums.isEmpty() && searchQuery.isNotEmpty() -> {
                        ModernEmptySearchState(searchQuery = searchQuery)
                    }
                    albums.isEmpty() -> {
                        ModernEmptyAlbumsState()
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredAlbums, key = { "${it.name}|${it.artist}" }) { album ->
                                ModernAlbumGridItem(
                                    album = album,
                                    onClick = { onAlbumClick(album) }
                                )
                            }

                            // Espace en bas pour le lecteur
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
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
                        "Rechercher des albums...",
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
                "Chargement des albums...",
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
                "Aucun album trouvé pour \"$searchQuery\"",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ModernEmptyAlbumsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "empty_albums")
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
                color = GRADIENT_COLORS[3].copy(alpha = 0.15f),
                modifier = Modifier
                    .size(96.dp)
                    .scale(scale)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Album,
                        contentDescription = null,
                        tint = GRADIENT_COLORS[3],
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Text(
                "Aucun album",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Text(
                "Ajoutez de la musique à votre bibliothèque\npour voir vos albums.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernAlbumGridItem(
    album: Album,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = ALBUM_CARD_GRADIENT
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                AlbumArtwork(
                    albumArtUri = album.tracks.firstOrNull()?.albumArtUri,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    cornerRadius = 12.dp,
                    iconSize = 56.dp,
                    gradientColors = listOf(
                        GRADIENT_COLORS[1].copy(alpha = 0.4f),
                        GRADIENT_COLORS[1].copy(alpha = 0.2f)
                    )
                )

                // Info album (hauteur fixe)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Nom de l'album - 2 lignes max
                    Text(
                        text = album.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().height(34.dp) // Hauteur fixe pour 2 lignes
                    )

                    // Artiste - 1 ligne
                    Text(
                        text = album.artist,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp
                        ),
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Info supplémentaire - 1 ligne
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (album.year > 0) {
                            Text(
                                text = "${album.year} • ",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp
                                ),
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                        Text(
                            text = "${album.trackCount} morceau${if (album.trackCount > 1) "x" else ""}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp
                            ),
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

private fun getSortOrderLabel(sortOrder: AlbumViewModel.SortOrder): String {
    return when (sortOrder) {
        AlbumViewModel.SortOrder.ALBUM_NAME -> "Nom"
        AlbumViewModel.SortOrder.ARTIST_NAME -> "Artiste"
        AlbumViewModel.SortOrder.YEAR_DESC -> "Année ↓"
        AlbumViewModel.SortOrder.YEAR_ASC -> "Année ↑"
        AlbumViewModel.SortOrder.TRACK_COUNT -> "Morceaux"
    }
}