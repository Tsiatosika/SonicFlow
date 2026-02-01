package com.example.sonicflow.data.repository

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import com.example.sonicflow.data.local.dao.TrackDao
import com.example.sonicflow.data.local.dao.WaveformDataDao
import com.example.sonicflow.data.local.database.entities.WaveformDataEntity
import com.example.sonicflow.domain.model.WaveformData
import com.example.sonicflow.domain.repository.WaveformRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sqrt

class WaveformRepositoryImpl @Inject constructor(
    private val waveformDataDao: WaveformDataDao,
    private val trackDao: TrackDao,
    @ApplicationContext private val context: Context
) : WaveformRepository {

    override suspend fun getWaveformData(trackId: Long): WaveformData? {
        return withContext(Dispatchers.IO) {
            try {
                val entity = waveformDataDao.getWaveformData(trackId)
                entity?.let {
                    WaveformData(
                        trackId = it.trackId,
                        amplitudes = it.amplitudes,
                        duration = it.duration
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("WaveformRepository", "Error getting waveform data", e)
                null
            }
        }
    }

    override suspend fun generateWaveformData(trackId: Long): WaveformData {
        return withContext(Dispatchers.IO) {
            try {
                // Vérifier si les données existent déjà
                val existingData = waveformDataDao.getWaveformData(trackId)
                if (existingData != null) {
                    return@withContext WaveformData(
                        trackId = existingData.trackId,
                        amplitudes = existingData.amplitudes,
                        duration = existingData.duration
                    )
                }

                // Récupérer le track de la base de données
                val trackEntity = trackDao.getTrackByIdSync(trackId)
                    ?: throw Exception("Track not found")

                // Extraire les données audio
                val amplitudes = extractAudioAmplitudes(trackEntity.uri)
                val duration = trackEntity.duration

                // Créer l'entité waveform
                val waveformEntity = WaveformDataEntity(
                    trackId = trackId,
                    amplitudes = amplitudes,
                    duration = duration
                )

                // Sauvegarder dans la base de données
                waveformDataDao.insert(waveformEntity)

                android.util.Log.d("WaveformRepository", "Generated waveform for track $trackId with ${amplitudes.size} samples")

                WaveformData(
                    trackId = trackId,
                    amplitudes = amplitudes,
                    duration = duration
                )
            } catch (e: Exception) {
                android.util.Log.e("WaveformRepository", "Error generating waveform", e)
                // Retourner des données par défaut en cas d'erreur
                WaveformData(
                    trackId = trackId,
                    amplitudes = generateDefaultWaveform(),
                    duration = 0L
                )
            }
        }
    }

    private fun extractAudioAmplitudes(audioUri: String): List<Float> {
        val extractor = MediaExtractor()

        try {
            extractor.setDataSource(context, android.net.Uri.parse(audioUri), null)

            // Trouver la piste audio
            var audioTrackIndex = -1
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    break
                }
            }

            if (audioTrackIndex == -1) {
                android.util.Log.w("WaveformRepository", "No audio track found")
                return generateDefaultWaveform()
            }

            extractor.selectTrack(audioTrackIndex)

            val format = extractor.getTrackFormat(audioTrackIndex)
            val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

            android.util.Log.d("WaveformRepository", "Sample rate: $sampleRate, Channels: $channelCount")

            val maxBufferSize = 256 * 1024 // 256 KB buffer
            val buffer = ByteBuffer.allocate(maxBufferSize)
            val amplitudesList = mutableListOf<Float>()

            // Nombre de samples à regrouper pour chaque point du waveform
            // On vise environ 200-300 points pour le waveform
            val samplesPerPoint = (sampleRate * channelCount) / 50 // ~0.02 secondes par point
            var sampleCount = 0
            var sumSquares = 0.0

            while (true) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break

                buffer.position(0)
                buffer.limit(sampleSize)

                // Lire les samples (en supposant 16-bit PCM)
                while (buffer.remaining() >= 2) {
                    val sample = buffer.short.toFloat() / Short.MAX_VALUE
                    sumSquares += (sample * sample).toDouble()
                    sampleCount++

                    if (sampleCount >= samplesPerPoint) {
                        val rms = sqrt(sumSquares / sampleCount).toFloat()
                        amplitudesList.add(rms.coerceIn(0f, 1f))
                        sumSquares = 0.0
                        sampleCount = 0
                    }
                }

                extractor.advance()
                buffer.clear()
            }

            // Ajouter le dernier point s'il reste des samples
            if (sampleCount > 0) {
                val rms = sqrt(sumSquares / sampleCount).toFloat()
                amplitudesList.add(rms.coerceIn(0f, 1f))
            }

            android.util.Log.d("WaveformRepository", "Extracted ${amplitudesList.size} amplitude points")

            // Si on a trop peu de points, générer un waveform par défaut
            if (amplitudesList.size < 10) {
                android.util.Log.w("WaveformRepository", "Too few amplitude points, using default")
                return generateDefaultWaveform()
            }

            // Normaliser les amplitudes
            val maxAmplitude = amplitudesList.maxOrNull() ?: 1f
            return if (maxAmplitude > 0f) {
                amplitudesList.map { (it / maxAmplitude).coerceIn(0f, 1f) }
            } else {
                generateDefaultWaveform()
            }

        } catch (e: Exception) {
            android.util.Log.e("WaveformRepository", "Error extracting audio amplitudes", e)
            return generateDefaultWaveform()
        } finally {
            extractor.release()
        }
    }

    private fun generateDefaultWaveform(points: Int = 200): List<Float> {
        // Générer une forme d'onde aléatoire mais réaliste
        return List(points) { i ->
            val progress = i.toFloat() / points
            val base = 0.3f + 0.4f * kotlin.math.sin(progress * Math.PI * 4).toFloat()
            val variation = (Math.random().toFloat() - 0.5f) * 0.3f
            (base + variation).coerceIn(0.1f, 1f)
        }
    }
}