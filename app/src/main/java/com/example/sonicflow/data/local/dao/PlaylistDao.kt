package com.example.sonicflow.data.local.dao

import androidx.room.*
import com.example.sonicflow.data.local.database.entities.PlaylistEntity
import com.example.sonicflow.data.local.database.entities.PlaylistTrackCrossRefEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylist(playlistId: Long): Flow<PlaylistEntity?>

    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Insert
    suspend fun addTrackToPlaylist(crossRef: PlaylistTrackCrossRefEntity)

    @Delete
    suspend fun removeTrackFromPlaylist(crossRef: PlaylistTrackCrossRefEntity)

    @Query("SELECT trackId FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    fun getTrackIdsForPlaylist(playlistId: Long): Flow<List<Long>>
}