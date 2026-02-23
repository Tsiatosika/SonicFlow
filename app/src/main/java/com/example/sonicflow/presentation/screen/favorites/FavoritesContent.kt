package com.example.sonicflow.presentation.screen.favorites

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sonicflow.domain.model.Track
import kotlinx.coroutines.delay
import kotlin.math.sin


private val SPECTRUM_COLORS = listOf(
    Color(0xFF9D00FF),  // Violet électrique
    Color(0xFFB700FF),  // Magenta vif (favoris)
    Color(0xFFD600FF),  // Rose violet
    Color(0xFF8B5CF6),  // Violet profond
    Color(0xFF06B6D4),  // Cyan électrique
    Color(0xFF00A3FF),  // Bleu vif
    Color(0xFF3B82F6)   // Bleu royal
)

@Composable
fun FavoritesContent(
    viewModel: FavoritesViewModel,
    onTrackClick: (Track) -> Unit,
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit
) {
    val favoriteTracks by viewModel.favoriteTracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentPlayingTrack by viewModel.currentPlayingTrack.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    var trackToRemove by remember { mutableStateOf<Track?>(null) }

    // Filtrer les favoris selon la recherche
    val filteredTracks = remember(favoriteTracks, searchQuery) {
        if (searchQuery.isEmpty()) {
            favoriteTracks
        } else {
            favoriteTracks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true) ||
                        it.album.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Animation pour le fond
    val infiniteTransition = rememberInfiniteTransition(label = "favorites_background")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )

    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            delay(100)
            focusRequester.requestFocus()
        } else {
            searchQuery = ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        // Background animé subtil
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.08f)) {
            val width = size.width
            val height = size.height

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SPECTRUM_COLORS[1].copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    radius = height * 0.6f
                ),
                radius = height * 0.6f,
                center = Offset(
                    x = width * 0.2f + sin(gradientOffset * 0.01f) * 30f,
                    y = height * 0.2f
                )
            )
        }

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

            // Titre principal (caché pendant la recherche)
            if (!isSearchActive) {
                Text(
                    text = "Mes favoris",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (favoriteTracks.isNotEmpty()) {
                    Text(
                        text = "${favoriteTracks.size} morceau${if (favoriteTracks.size > 1) "x" else ""}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White.copy(alpha = 0.6f)
                        ),
                    )
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
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = SPECTRUM_COLORS[1],
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    filteredTracks.isEmpty() && searchQuery.isNotEmpty() -> {
                        ModernEmptySearchState(searchQuery = searchQuery)
                    }
                    favoriteTracks.isEmpty() -> {
                        ModernEmptyFavoritesState()
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = if (isSearchActive) 8.dp else 0.dp,
                                bottom = 100.dp
                            )
                        ) {
                            // Résultats de recherche
                            if (searchQuery.isNotEmpty()) {
                                item {
                                    SearchResultHeader(
                                        count = filteredTracks.size,
                                        query = searchQuery
                                    )
                                }
                            }

                            // Liste des morceaux
                            items(filteredTracks, key = { it.id }) { track ->
                                FavoriteTrackItem(
                                    track = track,
                                    isCurrentlyPlaying = currentPlayingTrack?.id == track.id,
                                    onClick = {
                                        val index = favoriteTracks.indexOf(track)
                                        viewModel.playFavorites(index)
                                        onTrackClick(track)
                                    },
                                    onRemoveFavorite = {
                                        trackToRemove = track
                                    }
                                )
                            }
                        }
                    }
                }

                // FAB pour jouer tous les favoris
                if (favoriteTracks.isNotEmpty() && !isSearchActive) {
                    PlayAllFAB(
                        onClick = {
                            viewModel.playAllFavorites()
                            onTrackClick(favoriteTracks.first())
                        }
                    )
                }
            }
        }

        // Dialog: Retirer des favoris
        if (trackToRemove != null) {
            ModernRemoveFavoriteDialog(
                track = trackToRemove!!,
                onConfirm = {
                    viewModel.removeFromFavorites(trackToRemove!!.id)
                    trackToRemove = null
                },
                onDismiss = { trackToRemove = null }
            )
        }
    }
}

// ============================================================================
// COMPOSANTS MODERNES
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1A1A1A),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        "Rechercher dans les favoris...",
                        color = Color.White.copy(alpha = 0.5f)
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = SPECTRUM_COLORS[1],
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
            )

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultHeader(
    count: Int,
    query: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$count résultat${if (count > 1) "s" else ""}",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f)
            )
        )

        Surface(
            shape = RoundedCornerShape(100.dp),
            color = SPECTRUM_COLORS[1].copy(alpha = 0.15f),
            modifier = Modifier.padding(2.dp)
        ) {
            Text(
                text = "\"$query\"",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SPECTRUM_COLORS[1]
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun ModernEmptyFavoritesState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = SPECTRUM_COLORS[1].copy(alpha = 0.1f),
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = SPECTRUM_COLORS[1].copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Aucun favori",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Appuyez sur ♥ pour ajouter des morceaux à vos favoris",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ModernEmptySearchState(searchQuery: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.05f),
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Color.White.copy(alpha = 0.3f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Aucun résultat trouvé",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Aucun favori ne correspond à \"$searchQuery\"",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteTrackItem(
    track: Track,
    isCurrentlyPlaying: Boolean,
    onClick: () -> Unit,
    onRemoveFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "track_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isCurrentlyPlaying)
            SPECTRUM_COLORS[1].copy(alpha = 0.15f)
        else
            Color(0xFF1A1A1A),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Ligne 1 : Titre + Icône favoris
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isCurrentlyPlaying) FontWeight.Bold else FontWeight.Medium,
                        color = if (isCurrentlyPlaying) SPECTRUM_COLORS[1] else Color.White,
                        fontSize = 16.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Icône favoris (toujours remplie)
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Retirer des favoris",
                    tint = SPECTRUM_COLORS[1],
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onRemoveFavorite() }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Ligne 2 : Artiste - Album + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${track.artist} - ${track.album}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Menu (3 points)
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Plus d'options",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onRemoveFavorite() }
                )
            }

            // Indicateur de lecture en cours
            if (isCurrentlyPlaying) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(SPECTRUM_COLORS[1].copy(alpha = pulseAlpha))
                    )
                    Text(
                        text = "EN COURS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = SPECTRUM_COLORS[1].copy(alpha = 0.8f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayAllFAB(
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = SPECTRUM_COLORS[1],
            contentColor = Color.White,
            modifier = Modifier
                .size(64.dp)
                .scale(scale),
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Tout jouer",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun ModernRemoveFavoriteDialog(
    track: Track,
    onConfirm: () -> Unit,
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
                color = SPECTRUM_COLORS[1].copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = SPECTRUM_COLORS[1],
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        title = {
            Text(
                "Retirer des favoris ?",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column {
                Text(
                    "Le morceau suivant sera retiré :",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
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
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SPECTRUM_COLORS[1]
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Retirer",
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
