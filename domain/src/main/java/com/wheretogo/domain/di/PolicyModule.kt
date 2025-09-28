package com.wheretogo.domain.di

import com.wheretogo.domain.CheckpointCooldown
import com.wheretogo.domain.CommentCooldown
import com.wheretogo.domain.CoolDownPolicy
import com.wheretogo.domain.CourseCooldown
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
    @CourseCooldown
    @Provides
    fun provideCoursePolicy(): CoolDownPolicy {
        return CourseCooldown
    }

    @Singleton
    @CheckpointCooldown
    @Provides
    fun provideCheckpointPolicy(): CoolDownPolicy {
        return CheckpointCooldown
    }


    @Singleton
    @CommentCooldown
    @Provides
    fun provideCommentPolicy(): CoolDownPolicy {
        return CommentCooldown
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CourseCooldown

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CheckpointCooldown

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CommentCooldown

