package com.example.sonicflow.presentation.components

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Composant pour afficher l'artwork d'un album avec fallback sur une icône de musique
 *
 * @param albumArtUri URI de l'image d'album (peut être null)
 * @param size Taille du composant
 * @param cornerRadius Rayon des coins arrondis
 * @param iconSize Taille de l'icône de fallback
 * @param gradientColors Couleurs du gradient de fond (fallback)
 * @param modifier Modifier optionnel
 */
@Composable
fun AlbumArtwork(
    albumArtUri: Uri?,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    cornerRadius: Dp = 12.dp,
    iconSize: Dp = 32.dp,
    gradientColors: List<Color> = listOf(
        Color.White.copy(alpha = 0.2f),
        Color.White.copy(alpha = 0.1f)
    )
) {
    val context = LocalContext.current

    // Debug log
    Log.d("AlbumArtwork", "Loading artwork: $albumArtUri")

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.verticalGradient(gradientColors)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (albumArtUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(albumArtUri)
                    .crossfade(true)
                    .diskCacheKey(albumArtUri.toString())
                    .memoryCacheKey(albumArtUri.toString())
                    .build(),
                contentDescription = "Album artwork",
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(cornerRadius)),
                contentScale = ContentScale.Crop,
                onError = {
                    Log.e("AlbumArtwork", "Error loading image: $albumArtUri", it.result.throwable)
                },
                onSuccess = {
                    Log.d("AlbumArtwork", "Successfully loaded: $albumArtUri")
                }
            )
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Music",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/**
 * Variante avec String? pour la compatibilité
 * Convertit automatiquement le String en Uri
 */
@Composable
fun AlbumArtwork(
    albumArtUri: String?,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    cornerRadius: Dp = 12.dp,
    iconSize: Dp = 32.dp,
    gradientColors: List<Color> = listOf(
        Color.White.copy(alpha = 0.2f),
        Color.White.copy(alpha = 0.1f)
    )
) {
    val uri = albumArtUri?.let {
        try {
            Uri.parse(it)
        } catch (e: Exception) {
            Log.e("AlbumArtwork", "Error parsing URI: $it", e)
            null
        }
    }

    AlbumArtwork(
        albumArtUri = uri,
        modifier = modifier,
        size = size,
        cornerRadius = cornerRadius,
        iconSize = iconSize,
        gradientColors = gradientColors
    )
}