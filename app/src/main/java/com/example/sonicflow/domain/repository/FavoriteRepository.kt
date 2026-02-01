package com.example.sonicflow.domain.repository

import com.example.sonicflow.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getFavoriteTracks(): Flow<List<Track>>
    suspend fun addToFavorites(trackId: Long)
    suspend fun removeFromFavorites(trackId: Long)
    suspend fun isFavorite(trackId: Long): Boolean
}