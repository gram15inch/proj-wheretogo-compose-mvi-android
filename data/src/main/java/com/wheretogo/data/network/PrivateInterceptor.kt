package com.wheretogo.data.network;

import com.wheretogo.data.DataError
import com.wheretogo.data.datasource.AuthRemoteDatasource
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.nio.Buffer


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

// 디버깅용
fun Request.log(tag: String = "tst_"): Request {
    println("$tag ┌──────────────── REQUEST ────────────────")
    try { println("$tag │ ${method()} ${url()}") } catch (e: Exception) { println("$tag │ method/url 오류: ${e.message}") }
    try {
        println("$tag │ Headers:")
        val h = headers()
        for (i in 0 until h.size()) println("$tag │   ${h.name(i)}: ${h.value(i)}")
    } catch (e: Exception) { println("$tag │ Headers 오류: ${e.message}") }
    try {
        val buf = okio.Buffer()
        body()?.writeTo(buf)
        println("$tag │ Body: ${buf.readUtf8().ifBlank { "<empty>" }}")
    } catch (e: Exception) { println("$tag │ Body 오류: ${e.message}") }
    println("$tag └─────────────────────────────────────────")
    return this
}