package com.example.sonicflow.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val trackId: Long,
    val dateAdded: Long = System.currentTimeMillis()
)