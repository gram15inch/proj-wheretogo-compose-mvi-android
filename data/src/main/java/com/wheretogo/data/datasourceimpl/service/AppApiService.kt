package com.wheretogo.data.datasourceimpl.service

import com.wheretogo.data.model.firebase.MessageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface AppApiService {

    @GET("api/app/publicKey")
    suspend fun getPublicKey(
        @Header("x-access-key") apiAccessKey: String
    ): Response<MessageResponse>

    @GET("api/app/publicToken")
    suspend fun getPublicToken(
        @Header("x-app-signature") signature: String
    ): Response<MessageResponse>
}