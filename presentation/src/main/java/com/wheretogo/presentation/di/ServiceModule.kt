package com.wheretogo.presentation.di

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.wheretogo.presentation.AD_REFRESH_SIZE
import com.wheretogo.presentation.BuildConfig
import com.wheretogo.presentation.DEBUG_AD_REFRESH_SIZE
import com.wheretogo.domain.feature.LocationService
import com.wheretogo.presentation.PresentationBuildConfig
import com.wheretogo.presentation.feature.ads.AdService
import com.wheretogo.presentation.feature.ads.NativeAdServiceImpl
import com.wheretogo.presentation.feature.geo.LocationServiceImpl
import com.wheretogo.presentation.feature.map.MapOverlayService
import com.wheretogo.presentation.feature.map.MapOverlayServiceImpl
import com.wheretogo.presentation.feature.naver.NaverMapOverlayModifier
import com.wheretogo.presentation.feature.naver.NaverMapOverlayProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @SuppressLint("MissingPermission")
    @Singleton
    @Provides
    fun provideAdService(@ApplicationContext context: Context, buildConfig: PresentationBuildConfig): AdService {
        val refreshSize = if (BuildConfig.DEBUG) DEBUG_AD_REFRESH_SIZE else AD_REFRESH_SIZE
        return NativeAdServiceImpl(context, buildConfig.nativeAdId, refreshSize).apply {
            CoroutineScope(Dispatchers.IO).launch {
                MobileAds.initialize(context)
                if (buildConfig.isAdPreLoad)
                    refreshAd(1)
            }
        }
    }

    @Singleton
    @Provides
    fun provideLocationService(): LocationService {
        return LocationServiceImpl()
    }

    @Singleton
    @Provides
    fun provideNaverMapOverlayModifier(@ApplicationContext context: Context): NaverMapOverlayModifier {
        return NaverMapOverlayModifier(context, true)
    }


    @Provides
    fun provideNaverMapProvider(modifier2: NaverMapOverlayModifier): NaverMapOverlayProvider {
        return NaverMapOverlayProvider(modifier2)
    }

    @Provides
    fun provideMapOverlayServiceImpl(
        provider: NaverMapOverlayProvider,
        locationService: LocationService
    ): MapOverlayService {
        return MapOverlayServiceImpl(provider, locationService)
    }
}