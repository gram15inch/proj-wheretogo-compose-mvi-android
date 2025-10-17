package com.wheretogo.data.di

import android.content.Context
import com.wheretogo.data.datasourceimpl.store.SecureStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StoreModule {

    @Singleton
    @Provides
    fun provideSecureStore(@ApplicationContext context: Context): SecureStore {
        return SecureStore(context)
    }

}