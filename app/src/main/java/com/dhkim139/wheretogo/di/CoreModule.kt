package com.dhkim139.wheretogo.di

import android.content.Context
import com.dhkim139.wheretogo.device.DeviceStateServiceImpl
import com.wheretogo.domain.feature.DeviceStateService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    @Singleton
    @Provides
    fun provideDeviceStateService(
        @ApplicationContext context: Context
    ): DeviceStateService {
        return DeviceStateServiceImpl(
            context = context,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        )
    }
}