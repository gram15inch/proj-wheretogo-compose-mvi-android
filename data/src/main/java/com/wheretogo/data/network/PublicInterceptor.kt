package com.wheretogo.data.network;

import com.wheretogo.data.DataError
import com.wheretogo.data.feature.mapSuccess
import com.wheretogo.domain.feature.DataEncryptor
import com.wheretogo.domain.repository.AppRepository
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response


class PublicInterceptor @Inject constructor(
    private val appRepository: AppRepository,
    private val dataEncryptor: DataEncryptor
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val authResponse = chain.proceed(chain.request().authRequest())

        if (authResponse.code() != 401) return authResponse
        authResponse.close()

        val refreshed = runBlocking {
            appRepository.getPublicKey().mapSuccess { publicKey ->
                val signature = dataEncryptor.generateSignature(60 * 1000)
                val encrypted = dataEncryptor.encryptRsaBase64(signature, publicKey)
                appRepository.refreshPublicToken(encrypted)
            }
        }
        if (!refreshed.isSuccess) {
            throw DataError.PublicTokenInvalid("public token expire")
        }


        return chain.proceed(chain.request().authRequest())
    }

    private fun Request.authRequest(): Request {
        return newBuilder().apply {
            val token = runBlocking { getToken() }
            if (token != null) header("Authorization", "Bearer $token")
        }.build()
    }

    private suspend fun getToken(): String? {
        return appRepository.getPublicToken().getOrNull()
    }
}