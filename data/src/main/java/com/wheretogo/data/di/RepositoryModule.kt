package com.wheretogo.data.di

import com.wheretogo.data.repository.JourneyRepositoryImpl
import com.wheretogo.domain.repository.JourneyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindJourneyRepository(repositoryImpl: JourneyRepositoryImpl): JourneyRepository

}