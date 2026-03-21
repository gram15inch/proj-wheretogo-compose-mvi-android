package com.wheretogo.data.feature

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.wheretogo.data.model.firebase.DataResponse
import com.wheretogo.data.model.firebase.MessageResponse
import com.wheretogo.data.toDataError
import okhttp3.ResponseBody
import retrofit2.Response

fun Response<*>.safeErrorBody(): MessageResponse? {
    return if (!isSuccessful) {
        parseMessageBody(errorBody()) ?: MessageResponse(false, "errorBody 없음")
    } else {
        null
    }
}

private fun parseMessageBody(responseBody: ResponseBody?): MessageResponse? {
    return try {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val adapter: JsonAdapter<MessageResponse> = moshi.adapter(MessageResponse::class.java)
        val errorJson = responseBody?.string()
        val response = errorJson?.let { adapter.fromJson(it) }
        return response
    } catch (e: Exception) {
        null
    }
}

suspend fun <T> safeApiCall(
    call: suspend () -> Response<DataResponse<T>>
): Result<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception("네트워크의 결과값이 없습니다."))
            }
        } else {
            return Result.failure(response.toDataError())
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}