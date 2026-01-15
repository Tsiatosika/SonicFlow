package com.example.sonicflow.domain.usecase.track

import com.example.sonicflow.domain.model.Track
import com.example.sonicflow.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTracksUseCase @Inject constructor(
    private val trackRepository: TrackRepository
) {
    operator fun invoke(): Flow<List<Track>> = trackRepository.getAllTracks()
}