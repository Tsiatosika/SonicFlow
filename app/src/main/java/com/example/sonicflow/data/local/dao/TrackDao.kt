package com.example.sonicflow.data.local.dao

import androidx.room.*
import com.example.sonicflow.data.local.database.entities.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: TrackEntity): Long

    @Update
    suspend fun update(track: TrackEntity): Int

    @Delete
    suspend fun delete(track: TrackEntity): Int

    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :trackId")
    fun getTrackById(trackId: Long): Flow<TrackEntity?>

    // Méthode synchrone pour le WaveformRepository
    @Query("SELECT * FROM tracks WHERE id = :trackId")
    suspend fun getTrackByIdSync(trackId: Long): TrackEntity?

    @Query("SELECT * FROM tracks WHERE title LIKE '%' || :searchQuery || '%' OR artist LIKE '%' || :searchQuery || '%'")
    fun searchTracks(searchQuery: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY dateAdded DESC")
    fun getTracksByDateAdded(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun getTracksByName(): Flow<List<TrackEntity>>

    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getTrackCount(): Int

    @Query("DELETE FROM tracks")
    suspend fun deleteAll()
}