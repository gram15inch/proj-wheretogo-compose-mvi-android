package com.dhkim139.admin.wheretogo.di

import android.content.Context
import androidx.credentials.GetCredentialRequest
import com.dhkim139.admin.wheretogo.core.AdminApi
import com.dhkim139.admin.wheretogo.core.AdminNotification
import com.dhkim139.admin.wheretogo.feature.report.data.ReportRepository
import com.dhkim139.admin.wheretogo.feature.report.data.ReportRepositoryImpl
import com.google.android.libraries.identity.googleid.GetGoogleIdOption

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideAdminApi(
        @Named("privateRetrofit") retrofit: Retrofit,
    ): AdminApi = retrofit.create(AdminApi::class.java)

    @Provides
    @Singleton
    fun provideReportRepository(
        adminApi: AdminApi
    ): ReportRepository = ReportRepositoryImpl(adminApi)

    @Provides
    @Singleton
    fun provideAdminNotification(
        @ApplicationContext context: Context,
    ): AdminNotification = AdminNotification(context)


    @Provides
    @Singleton
    fun provideGetCredentialRequest(
        googleIdOption: GetGoogleIdOption,
    ): GetCredentialRequest =
        GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
}
