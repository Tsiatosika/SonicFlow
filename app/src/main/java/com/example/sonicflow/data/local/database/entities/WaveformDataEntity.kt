package com.example.sonicflow.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "waveform_data")
data class WaveformDataEntity(
    @PrimaryKey val trackId: Long,
    val amplitudes: List<Float>,
    val duration: Long
)