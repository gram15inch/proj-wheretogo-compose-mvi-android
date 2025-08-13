package com.wheretogo.data.network;

import com.wheretogo.data.datasource.AuthRemoteDatasource
import com.wheretogo.data.datasource.UserLocalDatasource
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response


class AuthInterceptor @Inject constructor(
    private val auth: AuthRemoteDatasource,
    private val user: UserLocalDatasource
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val authResponse = chain.proceed(chain.request().authRequest())

        if (authResponse.code() != 401) return authResponse
        authResponse.close()

        // 만료시 재시도
        val refreshed = runBlocking { refreshToken() }
        if (!refreshed) {
            runBlocking { logout() }
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

    private suspend fun logout() {
        user.clearUser()
        user.setRequestLogin(true)
    }
}