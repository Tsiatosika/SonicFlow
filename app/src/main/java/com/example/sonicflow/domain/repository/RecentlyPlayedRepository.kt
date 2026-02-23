package com.example.sonicflow.domain.repository

import com.example.sonicflow.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface RecentlyPlayedRepository {

    /**
     * Obtenir les morceaux récemment écoutés
     */
    fun getRecentlyPlayedTracks(limit: Int = 50): Flow<List<Track>>

    /**
     * Ajouter un morceau à l'historique (ou incrémenter le compteur)
     */
    suspend fun addToRecentlyPlayed(trackId: Long)

    /**
     * Supprimer un morceau de l'historique
     */
    suspend fun removeFromRecentlyPlayed(trackId: Long)

    /**
     * Effacer tout l'historique
     */
    suspend fun clearRecentlyPlayed()
}