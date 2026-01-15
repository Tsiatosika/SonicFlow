package com.example.sonicflow.domain.usecase.playlist

import com.example.sonicflow.domain.repository.PlaylistRepository
import javax.inject.Inject

class CreatePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(name: String): Long = playlistRepository.createPlaylist(name)
}