package com.example.sonicflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sonicflow.presentation.screen.library.LibraryScreen
import com.example.sonicflow.presentation.screen.library.LibraryViewModel
import com.example.sonicflow.presentation.screen.player.PlayerScreen
import com.example.sonicflow.presentation.screen.playlist.PlaylistScreen
import com.example.sonicflow.presentation.theme.SonicFlowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SonicFlowApp()
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
                    navController.navigate("${Screen.Player.route}/${track.id}")
                },
                onPlaylistClick = {
                    navController.navigate(Screen.Playlists.route)
                }
            )
        }

        composable(Screen.Playlists.route) {
            PlaylistScreen(
                onBackClick = { navController.navigateUp() }
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
    object Playlists : Screen("playlists")
    object Player : Screen("player")
}

@Preview(showBackground = true)
@Composable
fun SonicFlowAppPreview() {
    SonicFlowTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Prévisualisation simple
        }
    }
}