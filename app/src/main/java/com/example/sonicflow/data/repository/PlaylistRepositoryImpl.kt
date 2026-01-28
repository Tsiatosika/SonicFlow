package com.example.sonicflow.data.repository

import com.example.sonicflow.data.local.dao.PlaylistDao
import com.example.sonicflow.data.local.dao.PlaylistTrackCrossRefDao
import com.example.sonicflow.data.local.dao.TrackDao
import com.example.sonicflow.data.local.database.entities.PlaylistEntity
import com.example.sonicflow.data.local.database.entities.PlaylistTrackCrossRefEntity
import com.example.sonicflow.domain.model.Playlist
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val playlistTrackCrossRefDao: PlaylistTrackCrossRefDao,
    private val trackDao: TrackDao
) : PlaylistRepository {

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { entity ->
                val trackCount = playlistTrackCrossRefDao.getTrackCountForPlaylist(entity.id)
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    trackCount = trackCount,
                    createdAt = entity.dateCreated
                )
            }
        }
    }

    override fun getPlaylistWithTracks(playlistId: Long): Flow<PlaylistRepository.PlaylistWithTracks> {
        return combine(
            playlistDao.getAllPlaylists(),
            trackDao.getAllTracks()
        ) { playlists, tracks ->
            val playlist = playlists.find { it.id == playlistId }
            val trackIds = playlistTrackCrossRefDao.getTrackIdsForPlaylist(playlistId)
            val playlistTracks = tracks.filter { it.id in trackIds }
                .map { entity ->
                    Track(
                        id = entity.id,
                        title = entity.title,
                        artist = entity.artist,
                        album = entity.album,
                        duration = entity.duration,
                        uri = entity.uri,
                        albumArtUri = entity.albumArtUri ?: "",
                        path = entity.path,
                        size = entity.size,
                        dateAdded = entity.dateAdded,
                        displayName = entity.displayName,
                        mimeType = entity.mimeType
                    )
                }

            PlaylistRepository.PlaylistWithTracks(
                playlist = Playlist(
                    id = playlist?.id ?: 0,
                    name = playlist?.name ?: "",
                    trackCount = playlistTracks.size,
                    createdAt = playlist?.dateCreated ?: 0
                ),
                tracks = playlistTracks
            )
        }
    }

    override suspend fun createPlaylist(name: String): Long {
        val playlist = PlaylistEntity(
            name = name,
            dateCreated = System.currentTimeMillis(),
            dateModified = System.currentTimeMillis()
        )
        return playlistDao.insert(playlist)
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        val currentCount = playlistTrackCrossRefDao.getTrackCountForPlaylist(playlistId)
        val crossRef = PlaylistTrackCrossRefEntity(
            playlistId = playlistId,
            trackId = trackId,
            position = currentCount,
            dateAdded = System.currentTimeMillis()
        )
        playlistTrackCrossRefDao.insertRelation(crossRef)
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        playlistTrackCrossRefDao.removeTrackFromPlaylist(playlistId, trackId)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistTrackCrossRefDao.removeAllTracksFromPlaylist(playlistId)
        playlistDao.deletePlaylistById(playlistId)
    }
}