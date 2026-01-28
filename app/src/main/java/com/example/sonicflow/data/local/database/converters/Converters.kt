package com.example.sonicflow.data.local.database.converters

import androidx.room.TypeConverter

class Converters {

    // Convertisseur pour List<Float> (pour les waveforms)
    @TypeConverter
    fun fromFloatList(value: List<Float>?): String? {
        return value?.joinToString(separator = ",") { it.toString() }
    }

    @TypeConverter
    fun toFloatList(value: String?): List<Float>? {
        return value?.split(",")?.mapNotNull { it.toFloatOrNull() }
    }

    // Convertisseur pour List<String> (si nécessaire)
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(separator = "|")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split("|")?.filter { it.isNotEmpty() }
    }
}