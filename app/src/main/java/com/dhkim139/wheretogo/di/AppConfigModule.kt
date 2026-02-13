package com.dhkim139.wheretogo.di

import com.dhkim139.wheretogo.BuildConfig
import com.wheretogo.data.DataBuildConfig
import com.wheretogo.domain.model.app.AppBuildConfig
import com.wheretogo.presentation.PresentationBuildConfig
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
        val config = when (BuildConfig.BUILD_TYPE) {
            "release" -> {
                AppBuildConfig(
                    tmapAppKey = BuildConfig.TMAP_APP_KEY,
                    isCoolDown = true,
                    isCrashlytics = true,
                )
            }
            else -> {
                AppBuildConfig(
                    tmapAppKey = BuildConfig.TMAP_APP_KEY,
                    isCoolDown = false,
                    isCrashlytics = false,
                )
            }
        }
        return config
    }

    @Singleton
    @Provides
    fun providePresentationConfig(): PresentationBuildConfig{
        val testAdId = "ca-app-pub-3940256099942544/2247696110"
        val config = when (BuildConfig.BUILD_TYPE) {
            "release" -> {
                PresentationBuildConfig(
                    nativeAdId = testAdId, // todo 실제 앱 출시시 변경 // BuildConfig.NATIVE_AD_ID
                    isTestUi = false,
                    isAdPreLoad = true
                )
            }

            else -> {
                PresentationBuildConfig(
                    nativeAdId = testAdId,
                    isTestUi = true,
                    isAdPreLoad = false
                )
            }
        }
        return config
    }

    @Singleton
    @Provides
    fun provideDataConfig(): DataBuildConfig{
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
                    isTokenLog = true,
                    dbPrefix = "TEST_",
                )
            }
        }

        return config
    }




}