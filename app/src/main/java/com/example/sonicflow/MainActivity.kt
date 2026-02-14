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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sonicflow.presentation.screen.album.AlbumDetailScreen
import com.example.sonicflow.presentation.screen.album.AlbumViewModel
import com.example.sonicflow.presentation.screen.artist.ArtistDetailScreen
import com.example.sonicflow.presentation.screen.artist.ArtistViewModel
import com.example.sonicflow.presentation.screen.home.HomeScreen
import com.example.sonicflow.presentation.screen.library.LibraryViewModel
import com.example.sonicflow.presentation.screen.player.MiniPlayer
import com.example.sonicflow.presentation.screen.player.PlayerScreen
import com.example.sonicflow.presentation.screen.playlist.PlaylistDetailScreen
import com.example.sonicflow.presentation.theme.SonicFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder
import java.net.URLEncoder

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // ✅ Variable pour tracker si on doit recharger après permission
    private var shouldReloadAfterPermission = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            android.util.Log.d("MainActivity", "✅ Permission accordée")
            Toast.makeText(this, "Permission accordée - Chargement...", Toast.LENGTH_SHORT).show()
            shouldReloadAfterPermission = true
            // 🔥 FORCER LE RECHARGEMENT COMPLET
            recreate()
        } else {
            android.util.Log.e("MainActivity", "❌ Permission refusée")
            Toast.makeText(
                this,
                "Permission refusée - L'app ne peut pas accéder à vos fichiers audio",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("MainActivity", "onCreate - Checking permissions")
        enableEdgeToEdge()

        val hasPermission = checkAndRequestPermissions()
        android.util.Log.d("MainActivity", "Has permission: $hasPermission")

        setContent {
            SonicFlowApp(hasPermission = hasPermission)
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        return when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                android.util.Log.d("MainActivity", "✅ Permission already granted")
                true
            }
            shouldShowRequestPermissionRationale(permission) -> {
                android.util.Log.d("MainActivity", "⚠️ Showing rationale")
                Toast.makeText(
                    this,
                    "Cette permission est nécessaire pour lire vos fichiers audio",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(permission)
                false
            }
            else -> {
                android.util.Log.d("MainActivity", "📋 Requesting permission")
                requestPermissionLauncher.launch(permission)
                false
            }
        }
    }
}

@Composable
fun SonicFlowApp(hasPermission: Boolean) {
    android.util.Log.d("SonicFlowApp", "Rendering with permission: $hasPermission")

    SonicFlowTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavigation(hasPermission = hasPermission)
        }
    }
}

@Composable
fun AppNavigation(hasPermission: Boolean) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    Column(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.weight(1f)
        ) {
            // HOME avec 5 onglets
            composable(Screen.Home.route) {
                val libraryViewModel: LibraryViewModel = hiltViewModel()

                // 🔥 FORCER LE CHARGEMENT DES TRACKS SI ON A LA PERMISSION
                LaunchedEffect(hasPermission) {
                    if (hasPermission) {
                        android.util.Log.d("AppNavigation", "🔄 Loading tracks with permission")
                        libraryViewModel.loadTracks()
                    } else {
                        android.util.Log.d("AppNavigation", "⏸️ Waiting for permission")
                    }
                }

                HomeScreen(
                    onTrackClick = { track ->
                        libraryViewModel.playTrackFromList(track)
                        navController.navigate("${Screen.Player.route}/${track.id}")
                    },
                    onPlaylistDetailClick = { playlistId ->
                        navController.navigate("${Screen.PlaylistDetail.route}/$playlistId")
                    },
                    onArtistDetailClick = { artistName ->
                        val encoded = URLEncoder.encode(artistName, "UTF-8")
                        navController.navigate("${Screen.ArtistDetail.route}/$encoded")
                    },
                    onAlbumDetailClick = { albumName, artistName ->
                        val encodedAlbum = URLEncoder.encode(albumName, "UTF-8")
                        val encodedArtist = URLEncoder.encode(artistName, "UTF-8")
                        navController.navigate("${Screen.AlbumDetail.route}/$encodedAlbum/$encodedArtist")
                    }
                )
            }

            // ALBUM DETAIL - Reçoit albumName + artistName
            composable(
                route = "${Screen.AlbumDetail.route}/{albumName}/{artistName}",
                arguments = listOf(
                    navArgument("albumName") { type = NavType.StringType },
                    navArgument("artistName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val encodedAlbumName = backStackEntry.arguments?.getString("albumName") ?: ""
                val encodedArtistName = backStackEntry.arguments?.getString("artistName") ?: ""

                val albumName = URLDecoder.decode(encodedAlbumName, "UTF-8")
                val artistName = URLDecoder.decode(encodedArtistName, "UTF-8")

                val albumViewModel: AlbumViewModel = hiltViewModel()

                AlbumDetailScreen(
                    albumName = albumName,
                    artistName = artistName,
                    onBackClick = { navController.navigateUp() },
                    onTrackClick = { track ->
                        navController.navigate("${Screen.Player.route}/${track.id}")
                    },
                    viewModel = albumViewModel
                )
            }

            // ARTIST DETAIL - Reçoit artistName
            composable(
                route = "${Screen.ArtistDetail.route}/{artistName}",
                arguments = listOf(
                    navArgument("artistName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val encodedArtistName = backStackEntry.arguments?.getString("artistName") ?: ""
                val artistName = URLDecoder.decode(encodedArtistName, "UTF-8")

                val artistViewModel: ArtistViewModel = hiltViewModel()

                ArtistDetailScreen(
                    artistName = artistName,
                    onBackClick = { navController.navigateUp() },
                    onTrackClick = { track ->
                        navController.navigate("${Screen.Player.route}/${track.id}")
                    },
                    viewModel = artistViewModel
                )
            }

            // PLAYLIST DETAIL
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

            // PLAYER PLEIN ÉCRAN
            composable("${Screen.Player.route}/{trackId}") { backStackEntry ->
                val trackId = backStackEntry.arguments?.getString("trackId")?.toLongOrNull()
                PlayerScreen(
                    trackId = trackId,
                    onBackClick = { navController.navigateUp() }
                )
            }
        }

        // MINIPLAYER - Toujours rendu
        MiniPlayer(
            onPlayerClick = {
                navController.navigate("${Screen.Player.route}/0")
            },
            hideOnPlayer = currentRoute.startsWith(Screen.Player.route)
        )
    }
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AlbumDetail : Screen("album_detail")
    object ArtistDetail : Screen("artist_detail")
    object PlaylistDetail : Screen("playlist_detail")
    object Player : Screen("player")
}