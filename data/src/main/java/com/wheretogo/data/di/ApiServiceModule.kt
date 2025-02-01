package com.wheretogo.data.di

import com.wheretogo.data.datasourceimpl.service.NaverMapApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiServiceModule {

    @Singleton
    @Provides
    fun provideNaverMapApiService(retrofit: Retrofit): NaverMapApiService {
        return retrofit.create(NaverMapApiService::class.java)
    }

}