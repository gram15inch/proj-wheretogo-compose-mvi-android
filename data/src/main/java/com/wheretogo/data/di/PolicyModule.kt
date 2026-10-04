package com.wheretogo.data.di

import com.wheretogo.data.CachePolicy
import com.wheretogo.data.CheckpointPolicy
import com.wheretogo.data.CommentPolicy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class PolicyModule {

    @Singleton
    @CheckpointCache
    @Provides
    fun provideCheckpointPolicy(): CachePolicy {
        return CheckpointPolicy
    }


    @Singleton
    @CommentCache
    @Provides
    fun provideCommentPolicy(): CachePolicy {
        return CommentPolicy
    }
}


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CheckpointCache

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CommentCache