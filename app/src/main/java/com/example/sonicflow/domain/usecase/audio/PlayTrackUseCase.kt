package com.example.sonicflow.domain.usecase.audio

import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.AudioPlayerRepository
import javax.inject.Inject

class PlayTrackUseCase @Inject constructor(
    private val audioPlayerRepository: AudioPlayerRepository
) {
    suspend operator fun invoke(track: Track) = audioPlayerRepository.playTrack(track)
}