package com.example.sonicflow.presentation.screen.player

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import com.example.sonicflow.presentation.components.AlbumArtwork
import com.example.sonicflow.presentation.screen.waveform.WaveformViewModel
import com.example.sonicflow.presentation.screen.waveform.WaveformVisualizer

private val gradientColors = listOf(
    Color(0xFFE94560),
    Color(0xFFD946EF),
    Color(0xFF8B5CF6)
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
            // Top Bar moderne
            ModernTopBar(
                onBackClick = onBackClick,
                isFavorite = isFavorite,
                onFavoriteClick = {
                    currentTrack?.let { viewModel.toggleFavorite(it.id) }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 🎵 Album Art
            GlassAlbumArt(
                isPlaying = playbackState.isPlaying,
                albumArtUri = currentTrack?.albumArtUri,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 🎤 Track info
            ModernTrackInfo(
                title = currentTrack?.title ?: "No track",
                artist = currentTrack?.artist ?: "Unknown"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 🌊 Waveform
            if (waveformData != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(horizontal = 24.dp)
                ) {
                    WaveformVisualizer(
                        waveformData = waveformData,
                        currentPosition = playbackState.currentPosition,
                        duration = playbackState.duration,
                        onSeek = { position -> viewModel.seekTo(position) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 📊 Progress bar
            ModernProgressBar(
                currentPosition = playbackState.currentPosition,
                duration = playbackState.duration,
                onSeek = { viewModel.seekTo(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 🎛️ Control buttons - REMONTÉS
            ModernControlButtons(
                isPlaying = playbackState.isPlaying,
                isShuffleEnabled = isShuffleEnabled,
                repeatMode = repeatMode,
                onPlayPauseClick = { viewModel.togglePlayPause() },
                onPreviousClick = { viewModel.skipToPrevious() },
                onNextClick = { viewModel.skipToNext() },
                onShuffleClick = { viewModel.toggleShuffle() },
                onRepeatClick = { viewModel.toggleRepeatMode() }
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun AnimatedGradientBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset1"
    )

    val offset2 by infiniteTransition.animateFloat(
        initialValue = 1000f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(offset1, offset2),
                    end = Offset(offset2, offset1)
                )
            )
    )
}

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
        // Back button
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

        Spacer(modifier = Modifier.weight(1f))

        // Favorite button
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
                    contentDescription = "Favorite",
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
    albumArtUri: Uri?,
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

    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.15f),
            modifier = Modifier.fillMaxSize()
        ) {}

        Box(
            modifier = Modifier
                .fillMaxSize(0.9f)
                .rotate(if (isPlaying) rotation else 0f)
        ) {
            AlbumArtwork(
                albumArtUri = albumArtUri,
                modifier = Modifier.fillMaxSize(),
                size = 280.dp,
                cornerRadius = 140.dp,
                iconSize = 140.dp,
                gradientColors = listOf(
                    Color.White.copy(alpha = 0.3f),
                    Color.White.copy(alpha = 0.15f)
                )
            )
        }

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
                fontSize = 26.sp,
                color = Color.White
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = artist,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 17.sp,
                color = Color.White.copy(alpha = 0.8f)
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ModernProgressBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        var sliderPosition by remember { mutableStateOf(currentPosition.toFloat()) }
        var isUserSeeking by remember { mutableStateOf(false) }

        LaunchedEffect(currentPosition) {
            if (!isUserSeeking) {
                sliderPosition = currentPosition.toFloat()
            }
        }

        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
                isUserSeeking = true
            },
            onValueChangeFinished = {
                onSeek(sliderPosition.toLong())
                isUserSeeking = false
            },
            valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(sliderPosition.toLong()),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ModernControlButtons(
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
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle
        IconButton(
            onClick = onShuffleClick,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                tint = if (isShuffleEnabled) Color.White else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp)
            )
        }

        // Previous
        Surface(
            onClick = onPreviousClick,
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.15f),
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // Play/Pause - PLUS GROS
        val scale by animateFloatAsState(
            targetValue = if (isPlaying) 1.05f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "playPauseScale"
        )

        Surface(
            onClick = onPlayPauseClick,
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier
                .size(76.dp)
                .scale(scale)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = gradientColors[0],
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        // Next
        Surface(
            onClick = onNextClick,
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.15f),
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // Repeat
        IconButton(
            onClick = onRepeatClick,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                    else -> Icons.Default.Repeat
                },
                contentDescription = "Repeat",
                tint = if (repeatMode != Player.REPEAT_MODE_OFF) Color.White else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    if (millis < 0) return "0:00"
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format("%d:%02d", minutes, seconds)
}