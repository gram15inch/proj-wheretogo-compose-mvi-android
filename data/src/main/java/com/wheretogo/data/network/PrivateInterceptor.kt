package com.wheretogo.data.network;

import com.wheretogo.data.DataError
import com.wheretogo.data.datasource.AuthRemoteDatasource
import com.wheretogo.data.datasource.CheckPointLocalDatasource
import com.wheretogo.data.datasource.CommentLocalDatasource
import com.wheretogo.data.datasource.CourseLocalDatasource
import com.wheretogo.data.datasource.UserLocalDatasource
import com.wheretogo.domain.UserStatus
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response


class PrivateInterceptor @Inject constructor(
    private val auth: AuthRemoteDatasource,
    private val user: UserLocalDatasource,
    private val course: CourseLocalDatasource,
    private val checkPoint: CheckPointLocalDatasource,
    private val comment: CommentLocalDatasource,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val authResponse = chain.proceed(chain.request().authRequest())

        if (authResponse.code() != 401) return authResponse
        authResponse.close()

        // 만료시 재시도
        runBlocking {
            val refreshed = refreshToken()
            when (refreshed) {
                UserStatus.ACTIVE -> Unit
                UserStatus.NOT_LOGGED_IN -> {
                    throw DataError.UserInvalid("user not found for refresh")
                }

                else -> {
                    cacheClear()
                    throw DataError.Unauthorized("token expire: $refreshed")
                }
            }
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

    private suspend fun refreshToken(): UserStatus? {
        return auth.getUserStatus().getOrNull()
    }

    private suspend fun cacheClear(){
        user.clearUser()
        course.clear()
        checkPoint.clear()
        comment.clear()
        auth.signOutOnFirebase()
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