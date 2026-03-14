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
import kotlin.math.sin

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
    val allTracks by viewModel.allTracks.collectAsState()  // ✅ AJOUT
    val isLoading by viewModel.isLoading.collectAsState()
    val currentPlayingTrack by viewModel.currentPlayingTrack.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showAddTracksDialog by remember { mutableStateOf(false) }  // ✅ AJOUT
    var newName by remember { mutableStateOf("") }
    var selectedTrack by remember { mutableStateOf<Track?>(null) }
    var showRemoveTrackDialog by remember { mutableStateOf(false) }

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylist(playlistId)
        viewModel.loadAllTracks()  // ✅ AJOUT
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
                    // ✅ NOUVEAU: Bouton Ajouter des morceaux
                    IconButton(
                        onClick = { showAddTracksDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF10B981).copy(alpha = 0.2f),
                                        Color(0xFF06B6D4).copy(alpha = 0.2f)
                                    )
                                )
                            )
                            .padding(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add tracks",
                            tint = Color(0xFF10B981)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

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
                            radius = size.minDimension / 2 * fabScale
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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    LoadingState(gradient)
                }
                playlist == null -> {
                    ErrorState(
                        error = "Playlist introuvable",
                        gradient = gradient,
                        onRetry = { viewModel.loadPlaylist(playlistId) }
                    )
                }
                tracks.isEmpty() -> {
                    EmptyPlaylistState(
                        gradient = gradient,
                        onAddTracks = { showAddTracksDialog = true }  // ✅ AJOUT
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        // Header avec waveform animée
                        item {
                            PlaylistWaveformHeader(
                                playlistName = playlist!!.name,
                                trackCount = tracks.size,
                                gradient = gradient
                            )
                        }

                        // Liste des morceaux
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
                    }
                }
            }
        }

        // ✅ NOUVEAU: Dialog d'ajout de morceaux
        if (showAddTracksDialog) {
            AddTracksDialog(
                allTracks = allTracks,
                existingTrackIds = tracks.map { it.id }.toSet(),
                gradient = gradient,
                onDismiss = { showAddTracksDialog = false },
                onTracksSelected = { selectedTracks ->
                    selectedTracks.forEach { track ->
                        viewModel.addTrackToPlaylist(playlistId, track.id)
                    }
                    showAddTracksDialog = false
                }
            )
        }

        // Dialog de suppression
        if (showDeleteDialog) {
            ModernAlertDialog(
                title = "Supprimer la playlist ?",
                message = "Cette action est irréversible. Tous les morceaux de cette playlist seront perdus.",
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

        // Dialog de renommage
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

        // Dialog de retrait de morceau
        if (showRemoveTrackDialog && selectedTrack != null) {
            ModernAlertDialog(
                title = "Retirer ce morceau ?",
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

// ============================================================================
// ✅ NOUVEAU: Dialog d'ajout de morceaux avec design waveform
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTracksDialog(
    allTracks: List<Track>,
    existingTrackIds: Set<Long>,
    gradient: List<Color>,
    onDismiss: () -> Unit,
    onTracksSelected: (List<Track>) -> Unit
) {
    val availableTracks = remember(allTracks, existingTrackIds) {
        allTracks.filter { it.id !in existingTrackIds }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTracks by remember { mutableStateOf<Set<Track>>(emptySet()) }

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
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header avec waveform
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    gradient[0].copy(alpha = 0.15f),
                                    gradient[1].copy(alpha = 0.15f)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ajouter des morceaux",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = gradient[0]
                            )
                            if (selectedTracks.isNotEmpty()) {
                                Text(
                                    text = "${selectedTracks.size} sélectionné${if (selectedTracks.size > 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = gradient[1]
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(gradient[0].copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = gradient[0])
                        }
                    }
                }

                // SearchBar avec style waveform
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Rechercher...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = gradient[0])
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = gradient[0],
                        focusedLabelColor = gradient[0],
                        cursorColor = gradient[0]
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )

                // Liste
                Box(modifier = Modifier.weight(1f)) {
                    if (filteredTracks.isEmpty()) {
                        EmptySearchState(
                            searchQuery = searchQuery,
                            gradient = gradient
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(filteredTracks, key = { it.id }) { track ->
                                WaveformSelectableTrackItem(
                                    track = track,
                                    isSelected = track in selectedTracks,
                                    gradient = gradient,
                                    onToggleSelection = {
                                        selectedTracks = if (track in selectedTracks) {
                                            selectedTracks - track
                                        } else {
                                            selectedTracks + track
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Boutons d'action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedTracks = if (selectedTracks.size == filteredTracks.size) {
                                emptySet()
                            } else {
                                filteredTracks.toSet()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = filteredTracks.isNotEmpty(),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(gradient)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            if (selectedTracks.size == filteredTracks.size) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = gradient[0]
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (selectedTracks.size == filteredTracks.size) "Tout désél." else "Tout sél.",
                            color = gradient[0]
                        )
                    }

                    Button(
                        onClick = {
                            if (selectedTracks.isNotEmpty()) {
                                onTracksSelected(selectedTracks.toList())
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedTracks.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = gradient[0],
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ajouter (${selectedTracks.size})")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaveformSelectableTrackItem(
    track: Track,
    isSelected: Boolean,
    gradient: List<Color>,
    onToggleSelection: () -> Unit
) {
    Card(
        onClick = onToggleSelection,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                gradient[0].copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(16.dp)
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
                onCheckedChange = { onToggleSelection() },
                colors = CheckboxDefaults.colors(
                    checkedColor = gradient[0],
                    checkmarkColor = Color.White
                )
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) gradient[0].copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (isSelected) gradient[0] else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isSelected) gradient[0] else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${track.artist} • ${track.album}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptySearchState(
    searchQuery: String,
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
            color = gradient[0].copy(alpha = 0.1f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (searchQuery.isEmpty()) Icons.Default.MusicNote else Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = gradient[0]
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (searchQuery.isEmpty()) {
                "Tous les morceaux sont\ndéjà dans cette playlist"
            } else {
                "Aucun résultat"
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

// ============================================================================
// COMPOSANTS UI ORIGINAUX (CONSERVÉS)
// ============================================================================

@Composable
private fun PlaylistWaveformHeader(
    playlistName: String,
    trackCount: Int,
    gradient: List<Color>
) {
    // Animation de la waveform
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        // Fond avec gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            gradient[0].copy(alpha = 0.3f),
                            gradient[1].copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Waveform animée
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.BottomCenter)
        ) {
            val width = size.width
            val height = size.height
            val barCount = 40
            val barWidth = width / barCount

            for (i in 0 until barCount) {
                val normalizedI = i / barCount.toFloat()
                val barHeight = height * 0.5f * (0.3f + 0.7f * sin((normalizedI * 10f + wavePhase) * 0.05f).absoluteValue)

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            gradient[0].copy(alpha = 0.6f),
                            gradient[1].copy(alpha = 0.3f)
                        )
                    ),
                    topLeft = Offset(i * barWidth + barWidth * 0.2f, (height - barHeight) / 2),
                    size = androidx.compose.ui.geometry.Size(barWidth * 0.6f, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
            }
        }

        // Contenu
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Icône playlist avec effet glow
            Box {
                Canvas(modifier = Modifier.size(90.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                gradient[0].copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension / 2
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.Center)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.PlaylistPlay,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = playlistName,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = gradient[0]
                )
                Text(
                    text = "$trackCount morceau${if (trackCount > 1) "x" else ""}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun EmptyPlaylistState(
    gradient: List<Color>,
    onAddTracks: () -> Unit  // ✅ AJOUT
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icône avec effet waveform
        Box {
            Canvas(modifier = Modifier.size(150.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            gradient[0].copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    radius = size.minDimension / 2
                )
            }

            Surface(
                shape = CircleShape,
                color = gradient[0].copy(alpha = 0.1f),
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.Center)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = gradient[0]
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Playlist vide",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Ajoutez des morceaux pour\ncommencer à écouter",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ✅ Bouton d'ajout avec style waveform
        Button(
            onClick = onAddTracks,
            colors = ButtonDefaults.buttonColors(
                containerColor = gradient[0],
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Ajouter des morceaux",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun LoadingState(gradient: List<Color>) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = gradient[0],
                modifier = Modifier.size(48.dp)
            )
            Text(
                "Chargement...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    gradient: List<Color>,
    onRetry: () -> Unit
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