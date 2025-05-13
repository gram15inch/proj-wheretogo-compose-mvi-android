package com.wheretogo.data.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.wheretogo.data.FIREBASE_CLOUD_API_URL
import com.wheretogo.data.NAVER_OPEN_API_APIGW_URL
import com.wheretogo.data.NAVER_OPEN_API_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitClientModule {

    @Singleton
    @Provides
    fun provideClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10,TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Singleton
    @Provides
    @Named("apigw")
    fun provideNaverRetrofit(moshi: Moshi, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(
                MoshiConverterFactory.create(moshi)
            )
            .client(client)
            .baseUrl(NAVER_OPEN_API_APIGW_URL)
            .build()
    }

    @Singleton
    @Provides
    @Named("naver")
    fun provideXNaverRetrofit(moshi: Moshi, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(
                MoshiConverterFactory.create(moshi)
            )
            .client(client)
            .baseUrl(NAVER_OPEN_API_URL)
            .build()
    }

    @Singleton
    @Provides
    @Named("firebase")
    fun provideFirebaseRetrofit(moshi: Moshi, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(
                MoshiConverterFactory.create(moshi)
            )
            .client(client)
            .baseUrl(FIREBASE_CLOUD_API_URL)
            .build()
    }
}