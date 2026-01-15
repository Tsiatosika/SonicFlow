package com.example.sonicflow.data.repository

import com.example.sonicflow.data.local.dao.TrackDao
import com.example.sonicflow.data.local.database.entities.TrackEntity
import com.example.sonicflow.data.remote.mediastore.MediaStoreDataSource
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrackRepositoryImpl @Inject constructor(
    private val trackDao: TrackDao,
    private val mediaStoreDataSource: MediaStoreDataSource
) : TrackRepository {

    override fun getAllTracks(): Flow<List<Track>> {
        // On pourrait combiner la source locale (Room) et MediaStore, mais pour simplifier,
        // on utilise MediaStore pour scanner et on stocke en local pour les recherches.
        return trackDao.getAllTracks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchTracks(query: String): Flow<List<Track>> {
        return trackDao.searchTracks(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTrackById(id: Long): Flow<Track?> {
        return trackDao.getTrackById(id).map { entity ->
            entity?.toDomain()
        }
    }

    suspend fun refreshTracks() {
        val tracks = mediaStoreDataSource.queryAudioFiles()
        trackDao.insertTracks(tracks.map { it.toEntity() })
    }

    private fun TrackEntity.toDomain(): Track = Track(
        id = id,
        title = title,
        artist = artist,
        duration = duration,
        uri = uri,
        album = album,
        albumArtUri = albumArtUri
    )

    private fun Track.toEntity(): TrackEntity = TrackEntity(
        id = id,
        title = title,
        artist = artist,
        duration = duration,
        uri = uri,
        album = album,
        albumArtUri = albumArtUri
    )
}