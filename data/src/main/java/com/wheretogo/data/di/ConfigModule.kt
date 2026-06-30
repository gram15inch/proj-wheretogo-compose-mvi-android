package com.wheretogo.data.di

import com.wheretogo.data.ImageFormat
import com.wheretogo.data.model.confg.ImageConfig
import com.wheretogo.domain.ImageSize
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {
    private const val MAX_DOWN_MB = 10
    @Provides
    @Singleton
    fun provideImageConfig(): ImageConfig {
        return ImageConfig(
            format = ImageFormat.WEBP,
            maxDownMB = MAX_DOWN_MB,
            maxBitmapStreamCount = calculateStreamCount()
        )
    }
    private fun calculateStreamCount(): Int {
        val maxBound = ImageSize.entries.maxOf { maxOf(it.width, it.height) }
        val runtime = Runtime.getRuntime()
        val budgetBytes = runtime.maxMemory() / 5

        val streamBytes = maxBound * maxBound * 2 // 안전 마진
        return (budgetBytes / streamBytes).toInt().coerceIn(1, 12)
    }
}