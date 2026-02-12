package com.example.sonicflow.presentation.screen.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.example.sonicflow.presentation.screen.waveform.WaveformViewModel
import com.example.sonicflow.presentation.screen.waveform.WaveformVisualizer

// Couleurs du gradient moderne
private val gradientColors = listOf(
    Color(0xFFE94560),  // Rouge vibrant
    Color(0xFFD946EF),  // Magenta
    Color(0xFF8B5CF6)   // Violet profond
)

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    trackId: Long?,
    onBackClick: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
    waveformViewModel: WaveformViewModel = hiltViewModel()
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val waveformData by waveformViewModel.waveformData.collectAsState()

    // Charger le morceau si nécessaire
    LaunchedEffect(trackId) {
        if (trackId != null && trackId != 0L) {
            viewModel.loadAndPlayTrack(trackId)
            waveformViewModel.loadWaveform(trackId)
        }
    }

    LaunchedEffect(currentTrack?.id) {
        val id = currentTrack?.id
        if (id != null && id != 0L) {
            waveformViewModel.loadWaveform(id)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background gradient animé
        AnimatedGradientBackground()

        // Contenu principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar moderne (SANS "Now Playing")
            ModernTopBar(
                onBackClick = onBackClick,
                isFavorite = isFavorite,
                onFavoriteClick = {
                    currentTrack?.let { viewModel.toggleFavorite(it.id) }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Album Art rotatif avec effet glass
            GlassAlbumArt(
                isPlaying = playbackState.isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Track Info avec style moderne
            ModernTrackInfo(
                title = currentTrack?.title ?: "Loading...",
                artist = currentTrack?.artist ?: "Unknown Artist"
            )

            Spacer(modifier = Modifier.weight(1f))

            // Waveform
            WaveformVisualizer(
                waveformData = waveformData,
                currentPosition = playbackState.currentPosition,
                duration = playbackState.duration,
                onSeek = { position -> viewModel.seekTo(position) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Time labels
            TimeLabels(
                currentTime = viewModel.formatDuration(playbackState.currentPosition),
                totalTime = viewModel.formatDuration(playbackState.duration)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Contrôles modernes
            ModernControls(
                isPlaying = playbackState.isPlaying,
                isShuffleEnabled = isShuffleEnabled,
                repeatMode = repeatMode,
                onPlayPauseClick = { viewModel.togglePlayPause() },
                onPreviousClick = { viewModel.skipToPrevious() },
                onNextClick = { viewModel.skipToNext() },
                onShuffleClick = { viewModel.toggleShuffle() },
                onRepeatClick = { viewModel.toggleRepeatMode() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ "NOW PLAYING" EN BAS
            Text(
                text = "NOW PLAYING",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 2.sp,
                    color = Color.White.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AnimatedGradientBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")

    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(offset, offset),
                    end = Offset(offset + 800f, offset + 800f)
                )
            )
            .blur(100.dp)
    )

    // Overlay sombre pour lisibilité
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTopBar(
    onBackClick: () -> Unit,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button avec effet glass
        Surface(
            onClick = onBackClick,
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.15f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // ✅ SPACER pour centrer le bouton favori
        Spacer(modifier = Modifier.weight(1f))

        // Favorite button avec animation
        val scale by animateFloatAsState(
            targetValue = if (isFavorite) 1.2f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "favoriteScale"
        )

        Surface(
            onClick = onFavoriteClick,
            shape = CircleShape,
            color = if (isFavorite) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.15f),
            modifier = Modifier
                .size(44.dp)
                .scale(scale)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color(0xFFE94560) else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun GlassAlbumArt(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "albumRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        // Glow effect
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.1f),
            modifier = Modifier
                .fillMaxSize(0.95f)
                .blur(40.dp)
        ) {}

        // Main card avec glass effect
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.15f),
            modifier = Modifier
                .fillMaxSize(0.9f)
                .rotate(if (isPlaying) rotation else 0f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(140.dp),
                    tint = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Center dot (vinyl style)
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(60.dp)
        ) {}
    }
}

@Composable
private fun ModernTrackInfo(
    title: String,
    artist: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color.White
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = artist,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f)
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TimeLabels(
    currentTime: String,
    totalTime: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = currentTime,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            text = totalTime,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun ModernControls(
    isPlaying: Boolean,
    isShuffleEnabled: Boolean,
    repeatMode: Int,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle
        ModernIconButton(
            icon = Icons.Default.Shuffle,
            isActive = isShuffleEnabled,
            onClick = onShuffleClick,
            size = 48.dp
        )

        // ✅ Previous - PLUS GRAND
        ModernIconButton(
            icon = Icons.Default.SkipPrevious,
            isActive = false,
            onClick = onPreviousClick,
            size = 64.dp  // ✅ Agrandi
        )

        // ✅ Play/Pause - TRÈS GRAND
        ModernPlayPauseButton(
            isPlaying = isPlaying,
            onClick = onPlayPauseClick
        )

        // ✅ Next - PLUS GRAND
        ModernIconButton(
            icon = Icons.Default.SkipNext,
            isActive = false,
            onClick = onNextClick,
            size = 64.dp  // ✅ Agrandi
        )

        // Repeat
        ModernIconButton(
            icon = when (repeatMode) {
                Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                else -> Icons.Default.Repeat
            },
            isActive = repeatMode != Player.REPEAT_MODE_OFF,
            onClick = onRepeatClick,
            size = 48.dp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "iconScale"
    )

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isActive) Color.White.copy(alpha = 0.25f) else Color.Transparent,
        modifier = Modifier
            .size(size)
            .scale(scale)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isActive) Color.White else Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernPlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "playPauseScale"
    )

    // ✅ Animation de pulsation pendant la lecture
    val pulse by rememberInfiniteTransition(label = "pulse")
        .animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAnimation"
        )

    Surface(
        onClick = {
            isPressed = true
            onClick()
            isPressed = false
        },
        shape = CircleShape,
        color = Color.White,
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .scale(if (isPlaying) pulse else 1f),  // ✅ Pulse quand ça joue
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = gradientColors[0],
                modifier = Modifier.size(40.dp)
            )
        }
    }
}