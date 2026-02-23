package com.example.sonicflow.presentation.screen.recentlyplayed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sonicflow.domain.model.Track

// 🌈 PALETTE COULEURS
private val SPECTRUM_COLORS = listOf(
    Color(0xFF9D00FF),  // Violet électrique
    Color(0xFFB700FF),  // Magenta vif
    Color(0xFFD600FF),  // Rose violet
    Color(0xFF8B5CF6),  // Violet profond
    Color(0xFF06B6D4),  // Cyan électrique
    Color(0xFF00A3FF),  // Bleu vif
    Color(0xFF3B82F6)   // Bleu royal
)

private val GRADIENT_COLORS = listOf(
    Color(0xFF6366F1),
    Color(0xFF8B5CF6),
    Color(0xFFEC4899),
    Color(0xFFF97316)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyPlayedScreen(
    onBackClick: () -> Unit,
    onTrackClick: (Track) -> Unit,
    viewModel: RecentlyPlayedViewModel = hiltViewModel()
) {
    val recentlyPlayedTracks by viewModel.recentlyPlayedTracks.collectAsState()
    val currentPlayingTrack by viewModel.currentPlayingTrack.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }
    var selectedTrack by remember { mutableStateOf<Track?>(null) }
    var showTrackMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Écoutés récemment",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (recentlyPlayedTracks.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Tout effacer")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(GRADIENT_COLORS)
                )
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }
                recentlyPlayedTracks.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.White.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aucun historique",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Les morceaux que vous écoutez apparaîtront ici",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Header
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.15f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${recentlyPlayedTracks.size} morceaux",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Text(
                                            text = "Historique de lecture",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp),
                                        tint = SPECTRUM_COLORS[0]
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Liste des tracks
                        items(
                            items = recentlyPlayedTracks,
                            key = { it.id }
                        ) { track ->
                            SimpleTrackItem(
                                track = track,
                                isCurrentlyPlaying = currentPlayingTrack?.id == track.id,
                                isFavorite = false, // TODO: Implémenter
                                onFavoriteClick = {
                                    // TODO: Implémenter toggle favorite
                                },
                                onClick = {
                                    viewModel.playTrack(track)
                                    onTrackClick(track)
                                },
                                onMoreClick = {
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

    // Dialog pour effacer tout l'historique
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Effacer l'historique") },
            text = { Text("Voulez-vous effacer tout votre historique d'écoute ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllRecent()
                        showClearDialog = false
                    }
                ) {
                    Text("Effacer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Menu du track
    if (showTrackMenu && selectedTrack != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showTrackMenu = false
                selectedTrack = null
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = selectedTrack!!.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = selectedTrack!!.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Divider()

                ListItem(
                    headlineContent = { Text("Retirer de l'historique") },
                    leadingContent = {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        selectedTrack?.let { viewModel.removeFromRecent(it.id) }
                        showTrackMenu = false
                        selectedTrack = null
                    }
                )
            }
        }
    }
}

@Composable
fun SimpleTrackItem(
    track: Track,
    isCurrentlyPlaying: Boolean,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        color = Color.Transparent,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // ✅ LIGNE 1 : Titre du morceau + Favoris à droite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isCurrentlyPlaying) SPECTRUM_COLORS[0] else Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // ✅ BOUTON FAVORIS À DROITE DU TITRE
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                        tint = if (isFavorite) SPECTRUM_COLORS[1] else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // ✅ LIGNE 2 : Artiste - Album + Menu 3 points à droite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${track.artist} - ${track.album}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // ✅ BOUTON MENU (3 points)
                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Plus d'options",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
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
                            .background(SPECTRUM_COLORS[0])
                    )
                    Text(
                        text = "EN COURS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = SPECTRUM_COLORS[0].copy(alpha = 0.8f)
                        )
                    )
                }
            }
        }
    }
}