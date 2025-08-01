package com.wheretogo.data.di

import com.wheretogo.data.CachePolicy
import com.wheretogo.data.CheckpointPolicy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


import javax.inject.Qualifier


@Module
@InstallIn(SingletonComponent::class)
class PolicyModule {

    @Singleton
    @Checkpoint
    @Provides
    fun provideCheckpointPolicy(): CachePolicy {
        return CheckpointPolicy
    }
}


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Checkpoint
