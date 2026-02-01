package com.example.sonicflow.presentation.screen.waveform

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.sonicflow.domain.model.WaveformData
import kotlin.math.abs

@Composable
fun WaveformVisualizer(
    waveformData: WaveformData?,
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    waveformColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val amplitudes = waveformData?.amplitudes ?: emptyList()
    val progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val seekPosition = (offset.x / size.width * duration).toLong()
                    onSeek(seekPosition.coerceIn(0, duration))
                }
            }
    ) {
        if (amplitudes.isNotEmpty()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                val width = size.width
                val height = size.height
                val centerY = height / 2
                val barWidth = (width / amplitudes.size).coerceAtLeast(2f)
                val barSpacing = barWidth * 0.3f
                val actualBarWidth = barWidth - barSpacing
                val progressX = width * progress

                // Créer les gradients
                val playedGradient = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF00D9FF), // Cyan
                        Color(0xFF9D00FF), // Magenta
                        Color(0xFFFF00D6)  // Pink
                    ),
                    startX = 0f,
                    endX = progressX
                )

                val unplayedGradient = Brush.horizontalGradient(
                    colors = listOf(
                        waveformColor.copy(alpha = 0.3f),
                        waveformColor.copy(alpha = 0.5f)
                    ),
                    startX = progressX,
                    endX = width
                )

                amplitudes.forEachIndexed { index, amplitude ->
                    val x = index * barWidth + barSpacing / 2

                    // Hauteur de la barre basée sur l'amplitude
                    val normalizedAmplitude = amplitude.coerceIn(0.1f, 1f)
                    val barHeight = (normalizedAmplitude * height * 0.85f).coerceAtLeast(4f)

                    // Déterminer si la barre est dans la partie jouée ou non
                    val isPlayed = x < progressX

                    // Dessiner la barre avec des coins arrondis
                    drawRoundRect(
                        brush = if (isPlayed) playedGradient else unplayedGradient,
                        topLeft = Offset(x, centerY - barHeight / 2),
                        size = Size(actualBarWidth, barHeight),
                        cornerRadius = CornerRadius(actualBarWidth / 2, actualBarWidth / 2)
                    )
                }

                // Dessiner l'indicateur de position (ligne blanche)
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(progressX - 1.5f, 0f),
                    size = Size(3f, height),
                    cornerRadius = CornerRadius(1.5f, 1.5f)
                )
            }
        } else {
            // Afficher un waveform de placeholder si pas de données
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                val width = size.width
                val height = size.height
                val centerY = height / 2
                val barCount = 60
                val barWidth = (width / barCount).coerceAtLeast(2f)
                val barSpacing = barWidth * 0.3f
                val actualBarWidth = barWidth - barSpacing

                for (i in 0 until barCount) {
                    val x = i * barWidth + barSpacing / 2
                    val amplitude = (0.3f + 0.4f * kotlin.math.sin(i * 0.5f)).toFloat()
                    val barHeight = (amplitude * height * 0.7f).coerceAtLeast(4f)

                    drawRoundRect(
                        color = waveformColor.copy(alpha = 0.3f),
                        topLeft = Offset(x, centerY - barHeight / 2),
                        size = Size(actualBarWidth, barHeight),
                        cornerRadius = CornerRadius(actualBarWidth / 2, actualBarWidth / 2)
                    )
                }
            }
        }
    }
}
