package com.example.sonicflow.di

import android.content.Context
import androidx.media3.common.util.UnstableApi
import com.example.sonicflow.data.local.dao.PlaylistDao
import com.example.sonicflow.data.local.dao.PlaylistTrackCrossRefDao
import com.example.sonicflow.data.local.dao.TrackDao
import com.example.sonicflow.data.remote.mediastore.MediaStoreDataSource
import com.example.sonicflow.data.repository.AudioPlayerRepositoryImpl
import com.example.sonicflow.data.repository.PlaylistRepositoryImpl
import com.example.sonicflow.data.repository.TrackRepositoryImpl
import com.example.sonicflow.domain.repository.AudioPlayerRepository
import com.example.sonicflow.domain.repository.PlaylistRepository
import com.example.sonicflow.domain.repository.TrackRepository
import com.example.sonicflow.service.AudioPlayerServiceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTrackRepository(
        trackDao: TrackDao,
        mediaStoreDataSource: MediaStoreDataSource
    ): TrackRepository {
        return TrackRepositoryImpl(trackDao, mediaStoreDataSource)
    }

    @Provides
    @Singleton
    fun providePlaylistRepository(
        playlistDao: PlaylistDao,
        playlistTrackCrossRefDao: PlaylistTrackCrossRefDao,
        trackDao: TrackDao
    ): PlaylistRepository {
        return PlaylistRepositoryImpl(playlistDao, playlistTrackCrossRefDao, trackDao)
    }

    @UnstableApi
    @Provides
    @Singleton
    fun provideAudioPlayerServiceManager(
        @ApplicationContext context: Context
    ): AudioPlayerServiceManager {
        return AudioPlayerServiceManager(context).apply {
            bindService()
        }
    }

    @UnstableApi
    @Provides
    @Singleton
    fun provideAudioPlayerRepository(
        serviceManager: AudioPlayerServiceManager
    ): AudioPlayerRepository {
        return AudioPlayerRepositoryImpl(serviceManager)
    }
}