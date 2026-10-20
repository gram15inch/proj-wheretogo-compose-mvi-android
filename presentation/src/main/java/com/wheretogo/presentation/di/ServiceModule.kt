package com.wheretogo.presentation.di

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.wheretogo.domain.feature.LocationService
import com.wheretogo.presentation.PresentationBuildConfig
import com.wheretogo.presentation.feature.ads.AdService
import com.wheretogo.presentation.feature.ads.NativeAdConfig
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
        return NativeAdServiceImpl(context, buildConfig.nativeAdId, buildConfig, NativeAdConfig()).apply {
            CoroutineScope(Dispatchers.IO).launch {
                MobileAds.initialize(context)
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