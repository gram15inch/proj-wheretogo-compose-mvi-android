package com.wheretogo.data.di

import com.wheretogo.data.datasourceimpl.service.FirebaseApiService
import com.wheretogo.data.datasourceimpl.service.NaverFreeApiService
import com.wheretogo.data.datasourceimpl.service.NaverMapApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiServiceModule {

    @Singleton
    @Provides
    fun provideNaverMapApiService(@Named("apigw") retrofit: Retrofit): NaverMapApiService {
        return retrofit.create(NaverMapApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideNaverFreeApiService(@Named("naver") retrofit: Retrofit): NaverFreeApiService {
        return retrofit.create(NaverFreeApiService::class.java)
    }

    @Singleton
    @Provides
     fun provideFirebaseApiService(@Named("firebase") retrofit: Retrofit): FirebaseApiService {
        return retrofit.create(FirebaseApiService::class.java)
    }

}