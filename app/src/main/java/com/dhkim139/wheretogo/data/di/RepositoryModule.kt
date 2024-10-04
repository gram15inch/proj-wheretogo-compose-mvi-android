package com.dhkim139.wheretogo.data.di

import com.dhkim139.wheretogo.data.repository.MapRepositoryImpl
import com.dhkim139.wheretogo.domain.repository.MapRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindMapRepository(mapRepositoryImpl: MapRepositoryImpl): MapRepository

}