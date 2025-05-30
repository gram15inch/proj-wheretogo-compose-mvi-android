package com.dhkim139.wheretogo.di

import com.wheretogo.data.model.key.AppKey
import com.wheretogo.presentation.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object KeyModule {
    @Singleton
    @Provides
    fun provideAppKey(): AppKey {
        return AppKey(googleWebClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID_KEY)
    }

}