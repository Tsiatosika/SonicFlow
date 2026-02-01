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
import com.example.sonicflow.presentation.screen.favorites.FavoritesScreen
import com.example.sonicflow.presentation.screen.library.LibraryScreen
import com.example.sonicflow.presentation.screen.library.LibraryViewModel
import com.example.sonicflow.presentation.screen.player.PlayerScreen
import com.example.sonicflow.presentation.screen.playlist.PlaylistDetailScreen
import com.example.sonicflow.presentation.screen.playlist.PlaylistScreen
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

    NavHost(
        navController = navController,
        startDestination = Screen.Library.route
    ) {
        composable(Screen.Library.route) {
            val viewModel: LibraryViewModel = hiltViewModel()
            LibraryScreen(
                viewModel = viewModel,
                onTrackClick = { track ->
                    viewModel.playTrackFromList(track)
                    navController.navigate("${Screen.Player.route}/${track.id}")
                },
                onPlaylistClick = {
                    navController.navigate(Screen.Playlists.route)
                },
                onFavoritesClick = {
                    navController.navigate(Screen.Favorites.route)
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onBackClick = { navController.navigateUp() },
                onTrackClick = { track ->
                    navController.navigate("${Screen.Player.route}/${track.id}")
                }
            )
        }

        composable(Screen.Playlists.route) {
            PlaylistScreen(
                onBackClick = { navController.navigateUp() },
                onPlaylistClick = { playlistId ->
                    navController.navigate("${Screen.PlaylistDetail.route}/$playlistId")
                }
            )
        }

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
    object Library : Screen("library")
    object Favorites : Screen("favorites")
    object Playlists : Screen("playlists")
    object PlaylistDetail : Screen("playlist_detail")
    object Player : Screen("player")
}