package com.example.sonicflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sonicflow.data.local.database.entities.WaveformDataEntity

@Dao
interface WaveformDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(waveformData: WaveformDataEntity)

    @Query("SELECT * FROM waveform_data WHERE trackId = :trackId")
    suspend fun getWaveformData(trackId: Long): WaveformDataEntity?

    @Query("DELETE FROM waveform_data WHERE trackId = :trackId")
    suspend fun deleteWaveformData(trackId: Long)

    @Query("SELECT COUNT(*) FROM waveform_data WHERE trackId = :trackId")
    suspend fun exists(trackId: Long): Int
}