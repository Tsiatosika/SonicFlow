package com.example.sonicflow.di

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.example.sonicflow.data.local.dao.TrackDao
import com.example.sonicflow.data.remote.mediastore.MediaStoreDataSource
import com.example.sonicflow.domain.repository.AudioPlayerRepository
import com.example.sonicflow.domain.repository.TrackRepository
import com.example.sonicflow.data.repository.AudioPlayerRepositoryImpl
import com.example.sonicflow.data.repository.TrackRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideTrackRepository(
        dataSource: MediaStoreDataSource,
        trackDao: TrackDao
    ): TrackRepository {
        return TrackRepositoryImpl(trackDao, dataSource)
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideAudioPlayerRepository(
        audioPlayerService: com.example.sonicflow.service.AudioPlayerService
    ): AudioPlayerRepository {
        return AudioPlayerRepositoryImpl(audioPlayerService)
    }
}