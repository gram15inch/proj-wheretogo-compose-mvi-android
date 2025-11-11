package com.wheretogo.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FileModule {

    @Provides
    @Singleton
    fun provideImageFile(@ApplicationContext context: Context): File {
        return File(context.cacheDir, "")
    }

}