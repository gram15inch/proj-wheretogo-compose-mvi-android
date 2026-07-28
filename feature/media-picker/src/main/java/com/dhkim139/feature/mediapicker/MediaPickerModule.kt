package com.dhkim139.feature.mediapicker

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object MediaPickerModule {

    @Provides
    @Singleton
    fun provideGetImagesPageUseCase(@ApplicationContext context: Context): GetImagesPageUseCase{
        return GetImagesPageUseCaseImpl(context)
    }
}