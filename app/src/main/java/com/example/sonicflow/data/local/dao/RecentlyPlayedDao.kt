package com.example.sonicflow.data.local.dao

import androidx.room.*
import com.example.sonicflow.data.local.database.entities.RecentlyPlayedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyPlayedDao {

    @Query("""
        SELECT * FROM recently_played 
        ORDER BY playedAt DESC 
        LIMIT :limit
    """)
    fun getRecentlyPlayed(limit: Int = 50): Flow<List<RecentlyPlayedEntity>>

    @Query("""
        SELECT * FROM recently_played 
        WHERE trackId = :trackId 
        ORDER BY playedAt DESC 
        LIMIT 1
    """)
    suspend fun getLastPlayedEntry(trackId: Long): RecentlyPlayedEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: RecentlyPlayedEntity): Long

    @Update
    suspend fun update(entry: RecentlyPlayedEntity)

    @Query("DELETE FROM recently_played WHERE trackId = :trackId")
    suspend fun deleteByTrackId(trackId: Long)

    @Query("DELETE FROM recently_played")
    suspend fun clearAll()

    @Query("""
        DELETE FROM recently_played 
        WHERE id NOT IN (
            SELECT id FROM recently_played 
            ORDER BY playedAt DESC 
            LIMIT :keepCount
        )
    """)
    suspend fun keepOnlyRecent(keepCount: Int = 100)
}