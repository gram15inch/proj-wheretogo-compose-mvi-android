package com.wheretogo.data.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.wheretogo.data.NAVER_MAPS_NTRUSS_APIGW_URL
import com.wheretogo.data.NAVER_OPEN_API_URL
import com.wheretogo.data.firebaseApiUrlByBuild
import com.wheretogo.data.network.PrivateInterceptor
import com.wheretogo.data.network.PublicInterceptor
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
    @Named("apigw")
    fun provideNaverRetrofit(moshi: Moshi, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(
                MoshiConverterFactory.create(moshi)
            )
            .client(client)
            .baseUrl(NAVER_MAPS_NTRUSS_APIGW_URL)
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
    @Named("privateRetrofit")
    fun providePrivateFirebaseApiRetrofit(
        moshi: Moshi,
        @Named("privateHttp") client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .baseUrl(firebaseApiUrlByBuild())
            .build()
    }

    @Singleton
    @Provides
    @Named("publicRetrofit")
    fun providePublicFirebaseApiRetrofit(
        moshi: Moshi,
        @Named("publicHttp") client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .baseUrl(firebaseApiUrlByBuild())
            .build()
    }

    @Singleton
    @Provides
    fun provideDefaultRetrofit(
        moshi: Moshi,
        client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .baseUrl(firebaseApiUrlByBuild())
            .build()
    }


    // 레트로핏 유틸

    @Singleton
    @Provides
    @Named("privateHttp")
    fun providePrivateHttpClient(privateInterceptor: PrivateInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(privateInterceptor)
            .build()
    }

    @Singleton
    @Provides
    @Named("publicHttp")
    fun providePublicHttpClient(publicInterceptor: PublicInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(publicInterceptor)
            .build()
    }


    @Singleton
    @Provides
    fun provideHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
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
}