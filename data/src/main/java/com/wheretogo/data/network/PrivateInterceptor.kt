package com.wheretogo.data.network;

import com.wheretogo.data.DataError
import com.wheretogo.data.datasource.AuthRemoteDatasource
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response


class PrivateInterceptor @Inject constructor(
    private val auth: AuthRemoteDatasource
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val authResponse = chain.proceed(chain.request().authRequest())

        if (authResponse.code() != 401) return authResponse
        authResponse.close()

        // 만료시 재시도
        val refreshed = runBlocking { refreshToken() }
        if (!refreshed) {
            throw DataError.UserInvalid("refresh token expire")
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
        return auth.getApiToken(false).getOrNull()
    }

    private suspend fun refreshToken(): Boolean {
        return auth.getApiToken(true).isSuccess
    }
}