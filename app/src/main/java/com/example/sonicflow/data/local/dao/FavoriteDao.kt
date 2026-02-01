package com.example.sonicflow.data.local.dao

import androidx.room.*
import com.example.sonicflow.data.local.database.entities.FavoriteEntity
import com.example.sonicflow.data.local.database.entities.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE trackId = :trackId")
    suspend fun removeFavorite(trackId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE trackId = :trackId)")
    suspend fun isFavorite(trackId: Long): Boolean

    @Query("""
        SELECT tracks.* FROM tracks 
        INNER JOIN favorites ON tracks.id = favorites.trackId 
        ORDER BY favorites.dateAdded DESC
    """)
    fun getFavoriteTracks(): Flow<List<TrackEntity>>

    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun getFavoriteCount(): Int

    @Query("DELETE FROM favorites")
    suspend fun clearAllFavorites()
}