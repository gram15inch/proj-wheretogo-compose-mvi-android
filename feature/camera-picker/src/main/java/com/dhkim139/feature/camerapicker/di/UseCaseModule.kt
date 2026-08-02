package com.dhkim139.feature.camerapicker.di

import android.content.Context
import com.dhkim139.feature.camerapicker.usecase.CreateCaptureUriUseCase
import com.dhkim139.feature.camerapicker.usecase.CreateCaptureUriUseCaseImpl
import com.dhkim139.feature.camerapicker.usecase.VerifyImageUseCase
import com.dhkim139.feature.camerapicker.usecase.VerifyImageUseCaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideCreatePickerUriUseCase(@ApplicationContext context: Context): CreateCaptureUriUseCase{
        return CreateCaptureUriUseCaseImpl(context)
    }

    @Provides
    @Singleton
    fun provideVerifyImageUseCase(@ApplicationContext context: Context): VerifyImageUseCase {
        return VerifyImageUseCaseImpl(context)
    }

}