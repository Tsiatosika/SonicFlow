package com.example.sonicflow.presentation.screen.player

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    trackId: Long?,
    onBackClick: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()

    LaunchedEffect(trackId) {
        trackId?.let {
            viewModel.loadTrack(it)
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
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Album Art Placeholder
                Card(
                    modifier = Modifier
                        .size(280.dp)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Track Info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentTrack?.title ?: "No Track",
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentTrack?.artist ?: "Unknown Artist",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    var sliderPosition by remember { mutableFloatStateOf(0f) }
                    var isSliding by remember { mutableStateOf(false) }

                    LaunchedEffect(playbackState.currentPosition) {
                        if (!isSliding) {
                            sliderPosition = playbackState.currentPosition.toFloat()
                        }
                    }

                    Slider(
                        value = sliderPosition,
                        onValueChange = { value ->
                            isSliding = true
                            sliderPosition = value
                        },
                        onValueChangeFinished = {
                            viewModel.seekTo(sliderPosition.toLong())
                            isSliding = false
                        },
                        valueRange = 0f..playbackState.duration.toFloat().coerceAtLeast(1f),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = viewModel.formatDuration(playbackState.currentPosition),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = viewModel.formatDuration(playbackState.duration),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Playback Controls
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Shuffle
                    IconButton(
                        onClick = { viewModel.toggleShuffle() }
                    ) {
                        Icon(
                            Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffleEnabled)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    // Previous
                    IconButton(
                        onClick = { viewModel.skipToPrevious() }
                    ) {
                        Icon(
                            Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Play/Pause
                    FilledIconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Next
                    IconButton(
                        onClick = { viewModel.skipToNext() }
                    ) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "Next",
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Repeat
                    IconButton(
                        onClick = { viewModel.toggleRepeatMode() }
                    ) {
                        Icon(
                            when (repeatMode) {
                                Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = "Repeat",
                            tint = if (repeatMode != Player.REPEAT_MODE_OFF)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}