package com.wheretogo.data.feature

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.wheretogo.data.model.firebase.MessageResponse
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
