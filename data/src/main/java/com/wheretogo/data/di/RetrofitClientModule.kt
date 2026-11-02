package com.wheretogo.data.di

import com.wheretogo.data.DataBuildConfig
import com.wheretogo.data.network.PrivateInterceptor
import com.wheretogo.data.network.PrivateModuleInterceptor
import com.wheretogo.data.network.PublicInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitClientModule {
    val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    private val converterFactory = json.asConverterFactory("application/json".toMediaType())
    
    @Singleton
    @Provides
    @Named("naver")
    fun provideXNaverRetrofit(
        @Named("defaultHttp") client: OkHttpClient,
        buildConfig: DataBuildConfig
    ): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(converterFactory)
            .client(client)
            .baseUrl(buildConfig.naverOpenApiUrl)
            .build()
    }

    @Singleton
    @Provides
    @Named("privateRetrofit")
    fun providePrivateKotlinxFirebaseApiRetrofit(
        @Named("privateHttp") client: OkHttpClient,
        buildConfig: DataBuildConfig
    ): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(converterFactory)
            .client(client)
            .baseUrl(buildConfig.firebaseCloudApiUrl)
            .build()
    }

    @Singleton
    @Provides
    @Named("publicRetrofit")
    fun providePublicKotlinxFirebaseApiRetrofit(
        @Named("publicHttp") client: OkHttpClient,
        buildConfig: DataBuildConfig
    ): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(converterFactory)
            .client(client)
            .baseUrl(buildConfig.firebaseCloudApiUrl)
            .build()
    }

    @Singleton
    @Provides
    @Named("appRetrofit")
    fun provideAppRetrofit(
        @Named("defaultHttp") client: OkHttpClient,
        buildConfig: DataBuildConfig
    ): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(converterFactory)
            .client(client)
            .baseUrl(buildConfig.firebaseCloudApiUrl)
            .build()
    }

    // 모듈용
    @Singleton
    @Provides
    @Named("privateModuleRetrofit")
    fun providePrivateModuleFirebaseApiRetrofit(
        @Named("privateModuleHttp") client: OkHttpClient,
        buildConfig: DataBuildConfig
    ): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(converterFactory)
            .client(client)
            .baseUrl(buildConfig.firebaseCloudApiUrl)
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
    @Named("privateModuleHttp")
    fun providePrivateModuleHttpClient(privateModuleInterceptor: PrivateModuleInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(privateModuleInterceptor)
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
    @Named("defaultHttp")
    fun provideHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}