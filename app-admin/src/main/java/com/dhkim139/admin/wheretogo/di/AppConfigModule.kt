package com.dhkim139.admin.wheretogo.di

import com.dhkim139.admin.wheretogo.BuildConfig
import com.wheretogo.data.DataBuildConfig
import com.wheretogo.domain.model.app.AppBuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {
    @Singleton
    @Provides
    fun provideAppConfig(): AppBuildConfig {
        // 도메인 포함
        return AppBuildConfig(
            tmapAppKey = "",
            isCoolDown = false,
            isCrashlytics = false,
        )
    }

    @Singleton
    @Provides
    fun provideDataConfig(): DataBuildConfig {
        val config = when (BuildConfig.BUILD_TYPE) {
            "release" -> {
                DataBuildConfig(
                    firebaseCloudApiUrl = BuildConfig.FIREBASE_CLOUD_API_URL,
                    naverMapsNtrussApigwUrl = BuildConfig.NAVER_MAPS_NTRUSS_APIGW_URL,
                    naverOpenApiUrl = BuildConfig.NAVER_OPEN_API_URL,
                    googleWebClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID_KEY,
                    tokenRequestKey = BuildConfig.API_ACCESS_KEY,
                    naverMapsApigwClientIdKey = BuildConfig.NAVER_MAPS_APIGW_CLIENT_ID_KEY,
                    naverMapsApigwClientSecretkey = BuildConfig.NAVER_MAPS_APIGW_CLIENT_SECRET_KEY,
                    naverClientIdKey = BuildConfig.NAVER_CLIENT_ID_KEY,
                    naverClientSecretKey = BuildConfig.NAVER_CLIENT_SECRET_KEY,
                    isTokenLog = false,
                    dbPrefix = "RELEASE_"
                )
            }

            else -> {
                DataBuildConfig(
                    firebaseCloudApiUrl = BuildConfig.FIREBASE_CLOUD_STAGING_API_URL,
                    naverMapsNtrussApigwUrl = BuildConfig.NAVER_MAPS_NTRUSS_APIGW_URL,
                    naverOpenApiUrl = BuildConfig.NAVER_OPEN_API_URL,
                    googleWebClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID_KEY,
                    tokenRequestKey = BuildConfig.API_ACCESS_KEY,
                    naverMapsApigwClientIdKey = BuildConfig.NAVER_MAPS_APIGW_CLIENT_ID_KEY,
                    naverMapsApigwClientSecretkey = BuildConfig.NAVER_MAPS_APIGW_CLIENT_SECRET_KEY,
                    naverClientIdKey = BuildConfig.NAVER_CLIENT_ID_KEY,
                    naverClientSecretKey = BuildConfig.NAVER_CLIENT_SECRET_KEY,
                    isTokenLog = false,
                    dbPrefix = "TEST_",
                )
            }
        }

        return config
    }


}