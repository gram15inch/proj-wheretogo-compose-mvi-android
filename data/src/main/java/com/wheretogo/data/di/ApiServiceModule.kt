package com.wheretogo.data.di

import com.wheretogo.data.datasourceimpl.service.AppApiService
import com.wheretogo.data.datasourceimpl.service.ContentApiService
import com.wheretogo.data.datasourceimpl.service.CourseManageApi
import com.wheretogo.data.datasourceimpl.service.CourseSyncApi
import com.wheretogo.data.datasourceimpl.service.GuestApiService
import com.wheretogo.data.datasourceimpl.service.NaverFreeApiService
import com.wheretogo.data.datasourceimpl.service.ReportApiService
import com.wheretogo.data.datasourceimpl.service.RouteApi
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

    @Singleton
    @Provides
    fun provideReportApiService(@Named("privateRetrofit") retrofit: Retrofit): ReportApiService {
        return retrofit.create(ReportApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideCourseSyncApiService(@Named("publicRetrofit") retrofit: Retrofit): CourseSyncApi {
        return retrofit.create(CourseSyncApi::class.java)
    }

    @Singleton
    @Provides
    fun provideCourseManageApiService(@Named("privateRetrofit") retrofit: Retrofit): CourseManageApi {
        return retrofit.create(CourseManageApi::class.java)
    }

    @Singleton
    @Provides
    internal fun provideCourseRouteApiService(@Named("publicRetrofit") retrofit: Retrofit): RouteApi {
        return retrofit.create(RouteApi::class.java)
    }
}