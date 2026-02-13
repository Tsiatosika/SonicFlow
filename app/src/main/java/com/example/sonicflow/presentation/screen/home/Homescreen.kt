package com.example.sonicflow.presentation.screen.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.presentation.screen.album.AlbumContent
import com.example.sonicflow.presentation.screen.album.AlbumViewModel
import com.example.sonicflow.presentation.screen.artist.ArtistContent
import com.example.sonicflow.presentation.screen.artist.ArtistViewModel
import com.example.sonicflow.presentation.screen.favorites.FavoritesContent
import com.example.sonicflow.presentation.screen.favorites.FavoritesViewModel
import com.example.sonicflow.presentation.screen.library.LibraryContent
import com.example.sonicflow.presentation.screen.library.LibraryViewModel
import com.example.sonicflow.presentation.screen.playlist.PlaylistContent
import com.example.sonicflow.presentation.screen.playlist.PlaylistViewModel
import kotlin.math.sin

// 🎨 PALETTE MODERNE - GRADIENTS DYNAMIQUES
private val GRADIENT_BACKGROUND = listOf(
    Color(0xFF6366F1),  // Indigo
    Color(0xFF8B5CF6),  // Violet
    Color(0xFFEC4899),  // Rose
    Color(0xFFF97316)   // Orange
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onTrackClick: (Track) -> Unit,
    onPlaylistDetailClick: (Long) -> Unit,
    onArtistDetailClick: (String) -> Unit,
    onAlbumDetailClick: (String, String) -> Unit,
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    favoritesViewModel: FavoritesViewModel = hiltViewModel(),
    artistViewModel: ArtistViewModel = hiltViewModel(),
    albumViewModel: AlbumViewModel = hiltViewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var isSearchActive by remember { mutableStateOf(false) }

    // Animation pour le fond dynamique
    val infiniteTransition = rememberInfiniteTransition(label = "background_animation")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )

    val tabs = listOf(
        "Morceaux",
        "Albums",
        "Artistes",
        "Playlists"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 🌈 FOND GRADIENT ANIMÉ DYNAMIQUE
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val width = size.width
            val height = size.height

            // Gradient principal animé
            drawRect(
                brush = Brush.linearGradient(
                    colors = GRADIENT_BACKGROUND,
                    start = Offset(gradientOffset * 0.5f, 0f),
                    end = Offset(width + gradientOffset * 0.5f, height)
                )
            )

            // Cercles gradient pour effet de profondeur
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF06B6D4).copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    radius = height * 0.5f
                ),
                radius = height * 0.5f,
                center = Offset(
                    x = width * 0.8f + sin(gradientOffset * 0.002f) * 100f,
                    y = height * 0.3f
                )
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFEC4899).copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    radius = height * 0.4f
                ),
                radius = height * 0.4f,
                center = Offset(
                    x = width * 0.2f - sin(gradientOffset * 0.002f) * 80f,
                    y = height * 0.6f
                )
            )
        }

        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    // BARRE DU HAUT - Titre + Recherche
                    ModernTopBar(
                        title = "Ma Musique",
                        isSearchActive = isSearchActive,
                        onSearchClick = { isSearchActive = !isSearchActive }
                    )

                    // ❤️ GRANDE ICÔNE FAVORIS À GAUCHE DANS UN CARRÉ
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Surface(
                            onClick = {
                                selectedTabIndex = 4
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = "Favoris",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    // ONGLETS
                    ModernTabRow(
                        selectedTabIndex = selectedTabIndex,
                        tabs = tabs,
                        onTabSelected = { index ->
                            selectedTabIndex = index
                            isSearchActive = false
                        }
                    )
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTabIndex) {
                    0 -> {
                        LibraryContent(
                            viewModel = libraryViewModel,
                            onTrackClick = onTrackClick,
                            onAlbumClick = { albumName, artistName ->
                                onAlbumDetailClick(albumName, artistName)
                            },
                            onArtistClick = { artistName ->
                                onArtistDetailClick(artistName)
                            },
                            isSearchActive = isSearchActive,
                            onSearchActiveChange = { isSearchActive = it }
                        )
                    }
                    1 -> {
                        AlbumContent(
                            viewModel = albumViewModel,
                            onAlbumClick = { album ->
                                onAlbumDetailClick(album.name, album.artist)
                            },
                            isSearchActive = isSearchActive,
                            onSearchActiveChange = { isSearchActive = it }
                        )
                    }
                    2 -> {
                        ArtistContent(
                            viewModel = artistViewModel,
                            onArtistClick = { artist ->
                                onArtistDetailClick(artist.name)
                            },
                            isSearchActive = isSearchActive,
                            onSearchActiveChange = { isSearchActive = it }
                        )
                    }
                    3 -> {
                        PlaylistContent(
                            viewModel = playlistViewModel,
                            onPlaylistClick = onPlaylistDetailClick,
                            isSearchActive = isSearchActive,
                            onSearchActiveChange = { isSearchActive = it }
                        )
                    }
                    4 -> {
                        FavoritesContent(
                            viewModel = favoritesViewModel,
                            onTrackClick = onTrackClick,
                            isSearchActive = isSearchActive,
                            onSearchActiveChange = { isSearchActive = it }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTopBar(
    title: String,
    isSearchActive: Boolean,
    onSearchClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ✅ TITRE SIMPLE BLANC - SANS GRADIENT
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                letterSpacing = (-0.5).sp,
                color = Color.White
            )
        )

        // Bouton Search
        Surface(
            onClick = onSearchClick,
            shape = CircleShape,
            color = Color.White.copy(alpha = if (isSearchActive) 0.25f else 0.15f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (isSearchActive) "Fermer" else "Rechercher",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTabRow(
    selectedTabIndex: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.Transparent,
        edgePadding = 16.dp,
        // ✅ TRAIT BLANC SOULIGNÉ MODERNE
        indicator = { tabPositions ->
            if (tabPositions.isNotEmpty() && selectedTabIndex < tabPositions.size) {
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                        )
                )
            }
        },
        divider = {}
    ) {
        tabs.forEachIndexed { index, title ->
            val selected = selectedTabIndex == index

            Tab(
                selected = selected,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 16.sp,
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.5f)
                        )
                    )
                },
                selectedContentColor = Color.White,
                unselectedContentColor = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 14.dp)
            )
        }
    }
}