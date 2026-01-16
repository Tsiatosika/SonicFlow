package com.example.sonicflow.di

import com.example.sonicflow.domain.repository.TrackRepository
import com.example.sonicflow.domain.usecase.track.GetTracksUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun provideGetTracksUseCase(
        trackRepository: TrackRepository
    ): GetTracksUseCase {
        return GetTracksUseCase(trackRepository)
    }
}