package com.wheretogo.data.di

import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.wheretogo.data.DataBuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Singleton
    @Provides
    fun provideGoogleSignInOptions(
        buildConfig: DataBuildConfig
    ): GetGoogleIdOption {
        return GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(buildConfig.googleWebClientId)
            .setAutoSelectEnabled(true)
            //.setNonce(generateNonce())
            .build()
    }
}