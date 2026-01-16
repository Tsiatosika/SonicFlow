package com.example.sonicflow.domain.usecase.waveform

import com.example.sonicflow.domain.model.WaveformData
import com.example.sonicflow.domain.repository.WaveformRepository
import javax.inject.Inject

class GenerateWaveformUseCase @Inject constructor(
    private val waveformRepository: WaveformRepository
) {
    suspend operator fun invoke(trackId: Long): WaveformData =
        waveformRepository.generateWaveformData(trackId)
}