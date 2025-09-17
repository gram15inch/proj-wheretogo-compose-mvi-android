package com.wheretogo.presentation.di

import com.wheretogo.presentation.DefaultErrorHandler
import com.wheretogo.presentation.ViewModelErrorHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
class ErrorModule {

    @Provides
    fun provideErrorHandler(): ViewModelErrorHandler = DefaultErrorHandler()
}