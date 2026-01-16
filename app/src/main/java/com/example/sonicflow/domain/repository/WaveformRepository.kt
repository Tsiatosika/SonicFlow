package com.example.sonicflow.domain.repository

import com.example.sonicflow.domain.model.WaveformData

interface WaveformRepository {
    suspend fun getWaveformData(trackId: Long): WaveformData?
    suspend fun generateWaveformData(trackId: Long): WaveformData
}