package com.example.sonicflow.di

import android.content.ContentResolver
import android.content.Context
import androidx.room.Room
import com.example.sonicflow.data.local.database.AppDatabase
import com.example.sonicflow.data.local.dao.TrackDao
import com.example.sonicflow.data.remote.mediastore.MediaStoreDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "sonicflow_database"  // Utiliser le même nom que dans AppDatabase.kt
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTrackDao(database: AppDatabase): TrackDao {
        return database.trackDao()
    }

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun provideMediaStoreDataSource(contentResolver: ContentResolver): MediaStoreDataSource {
        return MediaStoreDataSource(contentResolver)
    }
}