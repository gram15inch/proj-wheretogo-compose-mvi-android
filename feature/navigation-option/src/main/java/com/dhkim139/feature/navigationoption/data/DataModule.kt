package com.dhkim139.feature.navigationoption.data

import com.dhkim139.feature.navigationoption.domain.ParkingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class DataModule {

    @Singleton
    @Provides
    fun provideParkingRepository(impl: ParkingRepositoryImpl): ParkingRepository {
        return impl
    }

    @Singleton
    @Provides
    fun provideGuestApiService(@Named("publicRetrofit") retrofit: Retrofit): NavOptionApi {
        return retrofit.create(NavOptionApi::class.java)
    }
}