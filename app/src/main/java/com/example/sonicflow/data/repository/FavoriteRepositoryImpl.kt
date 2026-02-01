package com.example.sonicflow.data.repository

import com.example.sonicflow.data.local.dao.FavoriteDao
import com.example.sonicflow.data.local.database.entities.FavoriteEntity
import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao
) : FavoriteRepository {

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return favoriteDao.getFavoriteTracks().map { entities ->
            entities.map { entity ->
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
        }
    }

    override suspend fun addToFavorites(trackId: Long) {
        android.util.Log.d("FavoriteRepository", "Adding track $trackId to favorites")
        val favorite = FavoriteEntity(trackId = trackId)
        favoriteDao.addFavorite(favorite)
    }

    override suspend fun removeFromFavorites(trackId: Long) {
        android.util.Log.d("FavoriteRepository", "Removing track $trackId from favorites")
        favoriteDao.removeFavorite(trackId)
    }

    override suspend fun isFavorite(trackId: Long): Boolean {
        return favoriteDao.isFavorite(trackId)
    }
}