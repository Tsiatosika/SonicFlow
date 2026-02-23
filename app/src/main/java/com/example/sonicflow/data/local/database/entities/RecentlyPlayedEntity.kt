package com.example.sonicflow.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recently_played")
data class RecentlyPlayedEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackId: Long,
    val playedAt: Long,
    val playCount: Int = 1
)