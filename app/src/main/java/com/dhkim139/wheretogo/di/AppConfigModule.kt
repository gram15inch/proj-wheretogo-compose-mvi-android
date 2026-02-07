package com.dhkim139.wheretogo.di

import com.dhkim139.wheretogo.BuildConfig
import com.wheretogo.data.FIREBASE_CLOUD_API_URL
import com.wheretogo.data.FIREBASE_CLOUD_STAGING_API_URL
import com.wheretogo.data.NAVER_MAPS_NTRUSS_APIGW_URL
import com.wheretogo.data.NAVER_OPEN_API_URL
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
        val testAdId = "ca-app-pub-3940256099942544/2247696110"

        val config = when (BuildConfig.BUILD_TYPE) {
            "release" -> {
                AppBuildConfig(
                    firebaseCloudApiUrl = FIREBASE_CLOUD_API_URL,
                    naverMapsNtrussApigwUrl = NAVER_MAPS_NTRUSS_APIGW_URL,
                    naverOpenApiUrl = NAVER_OPEN_API_URL,
                    googleWebClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID_KEY,
                    tokenRequestKey = BuildConfig.API_ACCESS_KEY,
                    naverMapsApigwClientIdKey = BuildConfig.NAVER_MAPS_APIGW_CLIENT_ID_KEY,
                    naverMapsApigwClientSecretkey = BuildConfig.NAVER_MAPS_APIGW_CLIENT_SECRET_KEY,
                    naverClientIdKey = BuildConfig.NAVER_CLIENT_ID_KEY,
                    naverClientSecretKey = BuildConfig.NAVER_CLIENT_SECRET_KEY,
                    tmapAppKey = BuildConfig.TMAP_APP_KEY,
                    nativeAdId = testAdId, // todo 실제 앱 출시시 변경 // BuildConfig.NATIVE_AD_ID
                    isCoolDown = true,
                    isCrashlytics = true,
                    isTokenLog = false,
                    isTestUi = false,
                    isAdPreLoad = true,
                    dbPrefix = "RELEASE_",
                )
            }

            "qa" -> {
                AppBuildConfig(
                    firebaseCloudApiUrl = FIREBASE_CLOUD_API_URL,
                    naverMapsNtrussApigwUrl = NAVER_MAPS_NTRUSS_APIGW_URL,
                    naverOpenApiUrl = NAVER_OPEN_API_URL,
                    googleWebClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID_KEY,
                    tokenRequestKey = BuildConfig.API_ACCESS_KEY,
                    naverMapsApigwClientIdKey = BuildConfig.NAVER_MAPS_APIGW_CLIENT_ID_KEY,
                    naverMapsApigwClientSecretkey = BuildConfig.NAVER_MAPS_APIGW_CLIENT_SECRET_KEY,
                    naverClientIdKey = BuildConfig.NAVER_CLIENT_ID_KEY,
                    naverClientSecretKey = BuildConfig.NAVER_CLIENT_SECRET_KEY,
                    tmapAppKey = BuildConfig.TMAP_APP_KEY,
                    nativeAdId = testAdId,
                    isCoolDown = false,
                    isCrashlytics = false,
                    isTokenLog = true,
                    isTestUi = false,
                    isAdPreLoad = false,
                    dbPrefix = "RELEASE_"
                )
            }

            else -> {
                AppBuildConfig(
                    firebaseCloudApiUrl = FIREBASE_CLOUD_STAGING_API_URL,
                    naverMapsNtrussApigwUrl = NAVER_MAPS_NTRUSS_APIGW_URL,
                    naverOpenApiUrl = NAVER_OPEN_API_URL,
                    googleWebClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID_KEY,
                    tokenRequestKey = BuildConfig.API_ACCESS_KEY,
                    naverMapsApigwClientIdKey = BuildConfig.NAVER_MAPS_APIGW_CLIENT_ID_KEY,
                    naverMapsApigwClientSecretkey = BuildConfig.NAVER_MAPS_APIGW_CLIENT_SECRET_KEY,
                    naverClientIdKey = BuildConfig.NAVER_CLIENT_ID_KEY,
                    naverClientSecretKey = BuildConfig.NAVER_CLIENT_SECRET_KEY,
                    tmapAppKey = BuildConfig.TMAP_APP_KEY,
                    nativeAdId = testAdId,
                    isCoolDown = false,
                    isCrashlytics = false,
                    isTokenLog = true,
                    isTestUi = true,
                    isAdPreLoad = false,
                    dbPrefix = "TEST_"
                )
            }
        }
        return config
    }

}