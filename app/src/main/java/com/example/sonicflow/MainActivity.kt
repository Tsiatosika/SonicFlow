package com.example.sonicflow

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sonicflow.domain.model.Album
import com.example.sonicflow.domain.model.Artist
import com.example.sonicflow.presentation.screen.album.AlbumDetailScreen
import com.example.sonicflow.presentation.screen.album.AlbumViewModel
import com.example.sonicflow.presentation.screen.artist.ArtistDetailScreen
import com.example.sonicflow.presentation.screen.artist.ArtistViewModel
import com.example.sonicflow.presentation.screen.home.HomeScreen
import com.example.sonicflow.presentation.screen.library.LibraryViewModel
import com.example.sonicflow.presentation.screen.player.PlayerScreen
import com.example.sonicflow.presentation.screen.playlist.PlaylistDetailScreen
import com.example.sonicflow.presentation.theme.SonicFlowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Permission accordée", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                this,
                "Permission refusée - L'app ne peut pas accéder à vos fichiers audio",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkAndRequestPermissions()

        setContent {
            SonicFlowApp()
        }
    }

    private fun checkAndRequestPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                android.util.Log.d("MainActivity", "Permission already granted")
            }
            shouldShowRequestPermissionRationale(permission) -> {
                Toast.makeText(
                    this,
                    "Cette permission est nécessaire pour lire vos fichiers audio",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(permission)
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}

@Composable
fun SonicFlowApp() {
    SonicFlowTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Shared state pour conserver les données
    var selectedArtist: Artist? = null
    var selectedAlbum: Album? = null

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Écran principal avec tabs (Morceaux, Albums, Artistes, Playlists, Favoris)
        composable(Screen.Home.route) {
            val libraryViewModel: LibraryViewModel = hiltViewModel()
            HomeScreen(
                onTrackClick = { track ->
                    libraryViewModel.playTrackFromList(track)
                    navController.navigate("${Screen.Player.route}/${track.id}")
                },
                onPlaylistDetailClick = { playlistId ->
                    navController.navigate("${Screen.PlaylistDetail.route}/$playlistId")
                },
                onArtistDetailClick = { artist ->
                    selectedArtist = artist
                    navController.navigate(Screen.ArtistDetail.route)
                },
                onAlbumDetailClick = { album ->
                    selectedAlbum = album
                    navController.navigate(Screen.AlbumDetail.route)
                }
            )
        }

        // Détail d'un album
        composable(Screen.AlbumDetail.route) {
            val album = selectedAlbum
            val albumViewModel: AlbumViewModel = hiltViewModel()

            if (album != null) {
                AlbumDetailScreen(
                    album = album,
                    onBackClick = { navController.navigateUp() },
                    onTrackClick = { track ->
                        navController.navigate("${Screen.Player.route}/${track.id}")
                    },
                    viewModel = albumViewModel
                )
            }
        }

        // Détail d'un artiste
        composable(Screen.ArtistDetail.route) {
            val artist = selectedArtist
            val artistViewModel: ArtistViewModel = hiltViewModel()

            if (artist != null) {
                ArtistDetailScreen(
                    artist = artist,
                    onBackClick = { navController.navigateUp() },
                    onTrackClick = { track ->
                        navController.navigate("${Screen.Player.route}/${track.id}")
                    },
                    viewModel = artistViewModel
                )
            }
        }

        // Détail d'une playlist
        composable(
            route = "${Screen.PlaylistDetail.route}/{playlistId}",
            arguments = listOf(
                navArgument("playlistId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
            PlaylistDetailScreen(
                playlistId = playlistId,
                onBackClick = { navController.navigateUp() },
                onTrackClick = { track ->
                    navController.navigate("${Screen.Player.route}/${track.id}")
                }
            )
        }

        // Player
        composable("${Screen.Player.route}/{trackId}") { backStackEntry ->
            val trackId = backStackEntry.arguments?.getString("trackId")?.toLongOrNull()
            PlayerScreen(
                trackId = trackId,
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AlbumDetail : Screen("album_detail")
    object ArtistDetail : Screen("artist_detail")
    object PlaylistDetail : Screen("playlist_detail")
    object Player : Screen("player")
}