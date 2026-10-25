package com.dhkim139.feature.navigationoption.data

import com.dhkim139.feature.navigationoption.domain.ParkingRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
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
    fun provideGuestApiService(@Named("parkingRetrofit") retrofit: Retrofit): NavOptionApi {
        return retrofit.create(NavOptionApi::class.java)
    }


    @Singleton
    @Provides
    @Named("parkingRetrofit")
    fun provideParkingRetrofit(
        @Named("publicRetrofit") retrofit: Retrofit
    ): Retrofit {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .callFactory(retrofit.callFactory())
            .baseUrl(retrofit.baseUrl())
            .build()
    }
}