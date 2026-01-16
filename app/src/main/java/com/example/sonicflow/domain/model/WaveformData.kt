package com.example.sonicflow.domain.model

data class WaveformData(
    val trackId: Long,
    val amplitudes: List<Float>,
    val duration: Long
)