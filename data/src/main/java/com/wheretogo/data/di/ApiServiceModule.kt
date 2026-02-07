package com.wheretogo.data.di

import com.wheretogo.data.datasourceimpl.service.AppApiService
import com.wheretogo.data.datasourceimpl.service.ContentApiService
import com.wheretogo.data.datasourceimpl.service.GuestApiService
import com.wheretogo.data.datasourceimpl.service.NaverFreeApiService
import com.wheretogo.data.datasourceimpl.service.NaverMapApiService
import com.wheretogo.data.datasourceimpl.service.UserApiService
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
    fun provideUserApiService(@Named("privateRetrofit") retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideContentApiService(@Named("privateRetrofit") retrofit: Retrofit): ContentApiService {
        return retrofit.create(ContentApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideGuestApiService(@Named("publicRetrofit") retrofit: Retrofit): GuestApiService {
        return retrofit.create(GuestApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideAppApiService(retrofit: Retrofit): AppApiService {
        return retrofit.create(AppApiService::class.java)
    }

}