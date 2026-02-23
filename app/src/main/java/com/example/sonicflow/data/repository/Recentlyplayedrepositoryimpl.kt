package com.example.sonicflow.data.repository

import android.net.Uri
import com.example.sonicflow.data.local.dao.RecentlyPlayedDao
import com.example.sonicflow.data.local.dao.TrackDao
import com.example.sonicflow.data.local.database.entities.RecentlyPlayedEntity
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.RecentlyPlayedRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecentlyPlayedRepositoryImpl @Inject constructor(
    private val recentlyPlayedDao: RecentlyPlayedDao,
    private val trackDao: TrackDao
) : RecentlyPlayedRepository {

    override fun getRecentlyPlayedTracks(limit: Int): Flow<List<Track>> {
        return combine(
            recentlyPlayedDao.getRecentlyPlayed(limit),
            trackDao.getAllTracks()
        ) { recentlyPlayed, allTracks ->
            if (allTracks.isEmpty()) return@combine emptyList()

            val trackMap = allTracks.associateBy { it.id }

            recentlyPlayed.mapNotNull { entry ->
                trackMap[entry.trackId]?.let { trackEntity ->
                    Track(
                        id = trackEntity.id,
                        title = trackEntity.title,
                        artist = trackEntity.artist,
                        album = trackEntity.album,
                        duration = trackEntity.duration,
                        uri = trackEntity.uri,
                        albumArtUri = trackEntity.albumArtUri?.let { Uri.parse(it) },
                        path = trackEntity.path,
                        size = trackEntity.size,
                        dateAdded = trackEntity.dateAdded,
                        displayName = trackEntity.displayName,
                        mimeType = trackEntity.mimeType,
                        year = trackEntity.year,
                        trackNumber = trackEntity.trackNumber
                    )
                }
            }
        }.flowOn(Dispatchers.IO) 
    }

    override suspend fun addToRecentlyPlayed(trackId: Long) {
        withContext(Dispatchers.IO) {
            try {
                val existingEntry = recentlyPlayedDao.getLastPlayedEntry(trackId)

                if (existingEntry != null) {
                    val updated = existingEntry.copy(
                        playedAt = System.currentTimeMillis(),
                        playCount = existingEntry.playCount + 1
                    )
                    recentlyPlayedDao.update(updated)
                    android.util.Log.d("RecentlyPlayedRepo", "Updated track $trackId, playCount=${updated.playCount}")
                } else {
                    val newEntry = RecentlyPlayedEntity(
                        trackId = trackId,
                        playedAt = System.currentTimeMillis(),
                        playCount = 1
                    )
                    recentlyPlayedDao.insert(newEntry)
                    android.util.Log.d("RecentlyPlayedRepo", "Inserted new entry for track $trackId")
                }

                // Garder seulement les 100 derniers
                recentlyPlayedDao.keepOnlyRecent(100)

            } catch (e: Exception) {
                android.util.Log.e("RecentlyPlayedRepo", "Error adding to recently played", e)
                throw e
            }
        }
    }

    override suspend fun removeFromRecentlyPlayed(trackId: Long) {
        withContext(Dispatchers.IO) {
            recentlyPlayedDao.deleteByTrackId(trackId)
        }
    }

    override suspend fun clearRecentlyPlayed() {
        withContext(Dispatchers.IO) {
            recentlyPlayedDao.clearAll()
        }
    }
}