package com.wheretogo.domain.di

import com.wheretogo.domain.BuildConfig
import com.wheretogo.domain.CheckpointCooldown
import com.wheretogo.domain.CommentCooldown
import com.wheretogo.domain.CoolDownPolicy
import com.wheretogo.domain.CourseCooldown
import com.wheretogo.domain.DefaultCoolDownPolicy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PolicyModule {
    private val debugCooldown = DefaultCoolDownPolicy(0)

    @Singleton
    @CourseCooldown
    @Provides
    fun provideCoursePolicy(): CoolDownPolicy {
        return if (BuildConfig.COOLDOWN) CourseCooldown else debugCooldown
    }

    @Singleton
    @CheckpointCooldown
    @Provides
    fun provideCheckpointPolicy(): CoolDownPolicy {
        return if (BuildConfig.COOLDOWN) CheckpointCooldown else debugCooldown
    }


    @Singleton
    @CommentCooldown
    @Provides
    fun provideCommentPolicy(): CoolDownPolicy {
        return if (BuildConfig.COOLDOWN) CommentCooldown else debugCooldown
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

