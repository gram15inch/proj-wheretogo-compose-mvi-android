package com.wheretogo.presentation.di

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.wheretogo.presentation.BuildConfig
import com.wheretogo.presentation.feature.ads.AdService
import com.wheretogo.presentation.feature.ads.NativeAdServiceImpl
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
object AdModule {

    @SuppressLint("MissingPermission")
    @Singleton
    @Provides
    fun provideAdService(@ApplicationContext context: Context): AdService {
        return NativeAdServiceImpl(context, BuildConfig.NATIVE_AD_ID).apply {
            CoroutineScope(Dispatchers.IO).launch {
                MobileAds.initialize(context)
                refreshAd()
            }
        }
    }
}