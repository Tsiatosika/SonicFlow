package com.example.sonicflow.presentation.screen.playlist

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.presentation.components.ModernTrackItem
import kotlin.math.absoluteValue

private val WAVEFORM_CYAN = Color(0xFF00D9FF)
private val WAVEFORM_MAGENTA = Color(0xFF9D00FF)
private val WAVEFORM_PINK = Color(0xFFFF00D6)
private val WAVEFORM_BLUE = Color(0xFF00A3FF)

private val playlistGradients = listOf(
    listOf(WAVEFORM_CYAN, WAVEFORM_BLUE),
    listOf(WAVEFORM_MAGENTA, WAVEFORM_PINK),
    listOf(WAVEFORM_BLUE, WAVEFORM_MAGENTA),
    listOf(WAVEFORM_PINK, WAVEFORM_CYAN),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    onBackClick: () -> Unit,
    onTrackClick: (Track) -> Unit,
    viewModel: PlaylistViewModel = hiltViewModel()
) {
    val playlist by viewModel.currentPlaylist.collectAsState()
    val tracks by viewModel.playlistTracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentPlayingTrack by viewModel.currentPlayingTrack.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var selectedTrack by remember { mutableStateOf<Track?>(null) }
    var showRemoveTrackDialog by remember { mutableStateOf(false) }

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylist(playlistId)
    }

    // Animation pour le FAB
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val fabScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_scale"
    )

    val fabAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_alpha"
    )

    // Gradient basé sur l'ID de la playlist
    val gradientIndex = (playlistId % playlistGradients.size).toInt().absoluteValue
    val gradient = playlistGradients[gradientIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = playlist?.name ?: "Playlist",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Bouton Renommer avec style waveform
                    IconButton(
                        onClick = {
                            newName = playlist?.name ?: ""
                            showRenameDialog = true
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        WAVEFORM_CYAN.copy(alpha = 0.2f),
                                        WAVEFORM_BLUE.copy(alpha = 0.2f)
                                    )
                                )
                            )
                            .padding(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = WAVEFORM_CYAN
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Bouton Supprimer
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        WAVEFORM_PINK.copy(alpha = 0.2f),
                                        WAVEFORM_MAGENTA.copy(alpha = 0.2f)
                                    )
                                )
                            )
                            .padding(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = WAVEFORM_PINK
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
        },
        floatingActionButton = {
            if (tracks.isNotEmpty()) {
                Box {
                    // Effet de pulse autour du FAB
                    Canvas(
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.Center)
                    ) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    gradient[0].copy(alpha = 0.3f * fabAlpha),
                                    gradient[1].copy(alpha = 0.1f * fabAlpha),
                                    Color.Transparent,
                                )
                        ),
                        //radius = size().minDimension / 2 * fabScale,
                     //   center = Offset(size().width() / 2, size.height / 2)
                        )
                    }

                    FloatingActionButton(
                        onClick = {
                            viewModel.playAllPlaylistTracks()
                            tracks.firstOrNull()?.let { onTrackClick(it) }
                        },
                        containerColor = gradient[0],
                        contentColor = Color.White,
                        modifier = Modifier
                            .scale(fabScale)
                            .size(64.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Play all",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background gradient animé
            AnimatedBackground(gradient = gradient)

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = gradient[0],
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Chargement de la playlist...",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                playlist == null -> {
                    ErrorState(
                        error = "Playlist introuvable",
                        onRetry = { viewModel.loadPlaylist(playlistId) },
                        gradient = gradient
                    )
                }
                tracks.isEmpty() -> {
                    EmptyPlaylistDetail(gradient = gradient)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Header avec gradient
                        item {
                            PlaylistHeader(
                                playlistName = playlist!!.name,
                                trackCount = tracks.size,
                                gradient = gradient
                            )
                        }

                        // Stats rapides
                        item {
                            QuickStats(
                                trackCount = tracks.size,
                                totalDuration = tracks.sumOf { it.duration },
                                gradient = gradient
                            )
                        }

                        // Titre de section
                        item {
                            SectionHeader(
                                title = "Morceaux",
                                count = tracks.size,
                                gradient = gradient
                            )
                        }

                        items(tracks, key = { it.id }) { track ->
                            ModernTrackItem(
                                track = track,
                                isCurrentlyPlaying = currentPlayingTrack?.id == track.id,
                                onClick = {
                                    val index = tracks.indexOf(track)
                                    viewModel.playPlaylistTracks(index)
                                    onTrackClick(track)
                                },
                                onMoreClick = {
                                    selectedTrack = track
                                    showRemoveTrackDialog = true
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }

                        // Spacer pour le FAB
                        item {
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        }

        if (showDeleteDialog) {
            ModernAlertDialog(
                title = "Supprimer la playlist ?",
                message = "Cette action est irréversible. Tous les morceaux seront retirés.",
                confirmText = "Supprimer",
                dismissText = "Annuler",
                isDestructive = true,
                gradient = gradient,
                onConfirm = {
                    viewModel.deletePlaylist(playlistId)
                    showDeleteDialog = false
                    onBackClick()
                },
                onDismiss = { showDeleteDialog = false }
            )
        }

        if (showRenameDialog) {
            ModernRenameDialog(
                currentName = newName,
                onNameChange = { newName = it },
                onConfirm = {
                    viewModel.renamePlaylist(playlistId, newName)
                    showRenameDialog = false
                },
                onDismiss = { showRenameDialog = false },
                gradient = gradient
            )
        }

        // Dialog: Retirer un morceau
        if (showRemoveTrackDialog && selectedTrack != null) {
            ModernAlertDialog(
                title = "Retirer de la playlist ?",
                message = "\"${selectedTrack!!.title}\" sera retiré de cette playlist.",
                confirmText = "Retirer",
                dismissText = "Annuler",
                isDestructive = false,
                gradient = gradient,
                onConfirm = {
                    viewModel.removeTrackFromPlaylist(playlistId, selectedTrack!!.id)
                    showRemoveTrackDialog = false
                },
                onDismiss = { showRemoveTrackDialog = false }
            )
        }
    }
}

@Composable
private fun AnimatedBackground(gradient: List<Color>) {
    val infiniteTransition = rememberInfiniteTransition(label = "background_shift")

    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset_x"
    )

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset_y"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        gradient[0].copy(alpha = 0.05f),
                        gradient[1].copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    start = Offset(offsetX, offsetY),
                    end = Offset(offsetX + 500f, offsetY + 500f)
                )
            )
    )
}

@Composable
private fun PlaylistHeader(
    playlistName: String,
    trackCount: Int,
    gradient: List<Color>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        // Background gradient avec forme organique
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
        ) {
            val width = size.width
            val height = size.height

            // Cercle flou 1
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        gradient[0].copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    radius = height * 0.8f
                ),
                radius = height * 0.8f,
                center = Offset(width * 0.2f, height * 0.3f)
            )

            // Cercle flou 2
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        gradient[1].copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    radius = height * 0.7f
                ),
                radius = height * 0.7f,
                center = Offset(width * 0.8f, height * 0.6f)
            )

            // Vagues waveform
            val barWidth = 4f
            val barSpacing = 8f
            for (i in 0..20) {
                val alpha = 0.1f - (i * 0.005f).coerceIn(0f, 0.1f)
                val yOffset = kotlin.math.sin(i * 0.5f) * 20f

                drawRoundRect(
                    color = gradient[0].copy(alpha = alpha),
                    topLeft = Offset(width - (i * barSpacing) - 20f, height * 0.3f + yOffset),
                    size = androidx.compose.ui.geometry.Size(barWidth, height * 0.4f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2, barWidth / 2)
                )
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon avec glow effect
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                gradient[0].copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = gradient[0],
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.PlaylistPlay,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Nom avec gradient
            Text(
                text = playlistName,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = gradient
                        )
                    )
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Count avec style
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "$trackCount morceau${if (trackCount > 1) "x" else ""}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                )

                // Durée totale si disponible
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.5f))
                )

                Text(
                    text = "${trackCount * 3} min", // Approximation
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}

@Composable
private fun QuickStats(
    trackCount: Int,
    totalDuration: Long,
    gradient: List<Color>
) {
    val minutes = (totalDuration / 1000 / 60).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Default.MusicNote,
                value = "$trackCount",
                label = "Morceaux",
                color = gradient[0]
            )

            StatItem(
                icon = Icons.Default.Timer,
                value = "${minutes}min",
                label = "Durée",
                color = gradient[1]
            )

            StatItem(
                icon = Icons.Default.Favorite,
                value = "0",
                label = "Favoris",
                color = WAVEFORM_PINK
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    gradient: List<Color>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = gradient
                        )
                    )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Surface(
            shape = CircleShape,
            color = gradient[0].copy(alpha = 0.1f),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = gradient[0]
                    )
                )
            }
        }
    }
}

@Composable
private fun EmptyPlaylistDetail(
    gradient: List<Color>
) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "empty_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale),
            contentAlignment = Alignment.Center
        ) {
            // Cercles animés
            Canvas(modifier = Modifier.matchParentSize()) {
                for (i in 0..2) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                gradient[i % 2].copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        ),
                        //radius = size().minDimension / 2 * (1f + i * 0.2f),
                       // center = Offset(size().width() / 2, size().height / 2)
                    )
                }
            }

            Icon(
                Icons.Default.PlaylistAdd,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = gradient[0]
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Cette playlist est vide",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.background(
                Brush.horizontalGradient(
                    colors = gradient
                )
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Ajoutez vos morceaux préférés pour créer la playlist parfaite",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { /* Naviguer vers bibliothèque */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = gradient[0]
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ajouter des morceaux")
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    gradient: List<Color>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = WAVEFORM_PINK.copy(alpha = 0.1f),
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = WAVEFORM_PINK
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Oups ! Une erreur est survenue",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = gradient[0]
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(56.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Réessayer")
        }
    }
}

@Composable
private fun ModernAlertDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    isDestructive: Boolean,
    gradient: List<Color>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = if (isDestructive) WAVEFORM_PINK else gradient[0],
        textContentColor = MaterialTheme.colorScheme.onSurface,
        icon = {
            Surface(
                shape = CircleShape,
                color = (if (isDestructive) WAVEFORM_PINK else gradient[0]).copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (isDestructive) Icons.Default.Warning else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (isDestructive) WAVEFORM_PINK else gradient[0],
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (isDestructive) WAVEFORM_PINK else gradient[0]
                )
            ) {
                Text(
                    text = confirmText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}

@Composable
private fun ModernRenameDialog(
    currentName: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    gradient: List<Color>
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = gradient[0],
        icon = {
            Surface(
                shape = CircleShape,
                color = gradient[0].copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = gradient[0],
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "Renommer la playlist",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column {
                Text(
                    "Donnez un nouveau nom à votre playlist",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = currentName,
                    onValueChange = onNameChange,
                    label = { Text("Nom de la playlist") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = gradient[0],
                        focusedLabelColor = gradient[0],
                        cursorColor = gradient[0]
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = currentName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = gradient[0],
                    contentColor = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    "Renommer",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Annuler")
            }
        }
    )
}
