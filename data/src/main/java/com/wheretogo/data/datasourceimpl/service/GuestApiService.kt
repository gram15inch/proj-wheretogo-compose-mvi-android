package com.wheretogo.data.datasourceimpl.service

import com.wheretogo.data.model.firebase.MessageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface GuestApiService {

    @GET("api/guest/")
    suspend fun getGuestSomething(
        @Header("X-api-key") apiKey: String,
    ): Response<MessageResponse>


}