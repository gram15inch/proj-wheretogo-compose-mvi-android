package com.wheretogo.data.di

import android.content.Context
import com.wheretogo.data.feature.DataEncryptorImpl
import com.wheretogo.domain.feature.DataEncryptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeatureModule {

    @Singleton
    @Provides
    fun provideSecureStore(@ApplicationContext context: Context): DataEncryptor {
        return DataEncryptorImpl(context)
    }

}