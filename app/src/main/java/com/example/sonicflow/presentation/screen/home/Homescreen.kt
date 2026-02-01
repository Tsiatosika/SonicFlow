package com.example.sonicflow.presentation.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sonicflow.domain.model.Artist
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.presentation.screen.artist.ArtistContent
import com.example.sonicflow.presentation.screen.artist.ArtistViewModel
import com.example.sonicflow.presentation.screen.favorites.FavoritesContent
import com.example.sonicflow.presentation.screen.favorites.FavoritesViewModel
import com.example.sonicflow.presentation.screen.library.LibraryContent
import com.example.sonicflow.presentation.screen.library.LibraryViewModel
import com.example.sonicflow.presentation.screen.playlist.PlaylistContent
import com.example.sonicflow.presentation.screen.playlist.PlaylistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onTrackClick: (Track) -> Unit,
    onPlaylistDetailClick: (Long) -> Unit,
    onArtistDetailClick: (Artist) -> Unit,
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    favoritesViewModel: FavoritesViewModel = hiltViewModel(),
    artistViewModel: ArtistViewModel = hiltViewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var isSearchActive by remember { mutableStateOf(false) }

    val tabs = listOf(
        TabItem(
            title = "Morceaux",
            icon = Icons.Default.MusicNote,
            selectedIcon = Icons.Default.MusicNote
        ),
        TabItem(
            title = "Artistes",
            icon = Icons.Default.Person,
            selectedIcon = Icons.Default.Person
        ),
        TabItem(
            title = "Playlists",
            icon = Icons.Default.PlaylistPlay,
            selectedIcon = Icons.Default.PlaylistPlay
        ),
        TabItem(
            title = "Favoris",
            icon = Icons.Default.Favorite,
            selectedIcon = Icons.Default.Favorite
        )
    )

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("SonicFlow") },
                    actions = {
                        // Bouton Search (disponible dans tous les onglets)
                        IconButton(onClick = { isSearchActive = !isSearchActive }) {
                            Icon(
                                if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (isSearchActive) "Close search" else "Search"
                            )
                        }

                        // Bouton Refresh (uniquement pour l'onglet Morceaux)
                        if (selectedTabIndex == 0) {
                            IconButton(onClick = { libraryViewModel.refreshTracks() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                            }
                        }
                    }
                )

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                selectedTabIndex = index
                                isSearchActive = false // Fermer la recherche en changeant d'onglet
                            },
                            text = { Text(tab.title) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTabIndex == index) tab.selectedIcon else tab.icon,
                                    contentDescription = tab.title
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTabIndex) {
                0 -> {
                    // Onglet Morceaux
                    LibraryContent(
                        viewModel = libraryViewModel,
                        onTrackClick = onTrackClick,
                        isSearchActive = isSearchActive,
                        onSearchActiveChange = { isSearchActive = it }
                    )
                }
                1 -> {
                    // Onglet Artistes
                    ArtistContent(
                        viewModel = artistViewModel,
                        onArtistClick = onArtistDetailClick,
                        isSearchActive = isSearchActive,
                        onSearchActiveChange = { isSearchActive = it }
                    )
                }
                2 -> {
                    // Onglet Playlists
                    PlaylistContent(
                        viewModel = playlistViewModel,
                        onPlaylistClick = onPlaylistDetailClick,
                        isSearchActive = isSearchActive,
                        onSearchActiveChange = { isSearchActive = it }
                    )
                }
                3 -> {
                    // Onglet Favoris
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

data class TabItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)