package com.wheretogo.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

/// 수정시 테스트를 위해 MockDataStoreModule 과 맞춤
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

    @Provides
    @Singleton
    fun provideUserDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.userDataStore
    }

    @Provides
    @Singleton
    fun provideImageFile(@ApplicationContext context: Context): File {
        return File(context.cacheDir, "")
    }
}