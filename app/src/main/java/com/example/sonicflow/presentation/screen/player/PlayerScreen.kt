package com.example.sonicflow.presentation.screen.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.example.sonicflow.presentation.screen.waveform.WaveformViewModel
import com.example.sonicflow.presentation.screen.waveform.WaveformVisualizer

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
    val isWaveformLoading by waveformViewModel.isLoading.collectAsState()

    LaunchedEffect(trackId) {
        trackId?.let {
            android.util.Log.d("PlayerScreen", "Loading track with ID: $it")
            viewModel.loadAndPlayTrack(it)
        }
    }

    LaunchedEffect(currentTrack) {
        currentTrack?.let { track ->
            android.util.Log.d("PlayerScreen", "Loading waveform for current track: ${track.id}")
            waveformViewModel.loadWaveform(track.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Now Playing") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Bouton Favoris avec animation
                    AnimatedFavoriteButton(
                        isFavorite = isFavorite,
                        onClick = {
                            currentTrack?.let { track ->
                                viewModel.toggleFavorite(track.id)
                            }
                        }
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Album Art ROTATIF géant
            RotatingAlbumArt(
                isPlaying = playbackState.isPlaying,
                size = 320.dp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Track Info avec animation
            AnimatedTrackInfo(
                title = currentTrack?.title ?: "Loading...",
                artist = currentTrack?.artist ?: "Unknown Artist"
            )

            Spacer(modifier = Modifier.weight(1f))

            // Waveform
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isWaveformLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                } else {
                    WaveformVisualizer(
                        waveformData = waveformData,
                        currentPosition = playbackState.currentPosition,
                        duration = playbackState.duration,
                        onSeek = { position ->
                            viewModel.seekTo(position)
                        },
                        waveformColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = viewModel.formatDuration(playbackState.currentPosition),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = viewModel.formatDuration(playbackState.duration),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Playback Controls avec animations
            AnimatedPlaybackControls(
                isPlaying = playbackState.isPlaying,
                isShuffleEnabled = isShuffleEnabled,
                repeatMode = repeatMode,
                onPlayPauseClick = { viewModel.togglePlayPause() },
                onPreviousClick = { viewModel.skipToPrevious() },
                onNextClick = { viewModel.skipToNext() },
                onShuffleClick = { viewModel.toggleShuffle() },
                onRepeatClick = { viewModel.toggleRepeatMode() }
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun RotatingAlbumArt(
    isPlaying: Boolean,
    size: androidx.compose.ui.unit.Dp
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

    Card(
        modifier = Modifier
            .size(size)
            .rotate(if (isPlaying) rotation else 0f),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(140.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AnimatedTrackInfo(
    title: String,
    artist: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        AnimatedContent(
            targetState = title,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            },
            label = "titleAnimation"
        ) { currentTitle ->
            Text(
                text = currentTitle,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedContent(
            targetState = artist,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            },
            label = "artistAnimation"
        ) { currentArtist ->
            Text(
                text = currentArtist,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AnimatedFavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "favoriteScale"
    )

    IconButton(onClick = onClick) {
        Icon(
            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.scale(scale)
        )
    }
}

@Composable
private fun AnimatedPlaybackControls(
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
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Shuffle
        AnimatedIconButton(
            onClick = onShuffleClick,
            icon = Icons.Default.Shuffle,
            isActive = isShuffleEnabled,
            size = 48.dp
        )

        // Previous
        AnimatedIconButton(
            onClick = onPreviousClick,
            icon = Icons.Default.SkipPrevious,
            size = 56.dp
        )

        // Play/Pause (grand bouton pulsant)
        PulsingPlayButton(
            isPlaying = isPlaying,
            onClick = onPlayPauseClick
        )

        // Next
        AnimatedIconButton(
            onClick = onNextClick,
            icon = Icons.Default.SkipNext,
            size = 56.dp
        )

        // Repeat
        AnimatedIconButton(
            onClick = onRepeatClick,
            icon = when (repeatMode) {
                Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                else -> Icons.Default.Repeat
            },
            isActive = repeatMode != Player.REPEAT_MODE_OFF,
            size = 48.dp
        )
    }
}

@Composable
private fun AnimatedIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean = false,
    size: androidx.compose.ui.unit.Dp = 48.dp
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconScale"
    )

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(size)
            .scale(scale)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(size * 0.6f),
            tint = if (isActive)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun PulsingPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val scale = if (isPlaying) pulseScale else 1f

    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .size(80.dp)
            .scale(scale),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
            },
            label = "playPauseIcon"
        ) { playing ->
            Icon(
                if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (playing) "Pause" else "Play",
                modifier = Modifier.size(44.dp)
            )
        }
    }
}