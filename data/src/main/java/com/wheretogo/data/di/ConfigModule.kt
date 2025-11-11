package com.wheretogo.data.di

import com.wheretogo.data.ImageFormat
import com.wheretogo.data.model.confg.ImageConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {

    @Provides
    @Singleton
    fun provideImageConfig(): ImageConfig {
        return ImageConfig(
            format = ImageFormat.WEBP,
            maxDownMB = 10
        )
    }

}