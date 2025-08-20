package com.wheretogo.data.datasourceimpl.service

import retrofit2.Response
import com.wheretogo.data.model.firebase.MessageResponse
import retrofit2.http.DELETE
import retrofit2.http.Path

interface FirebaseApiService {
    @DELETE("api/user/{userId}")
    suspend fun deleteUser(
        @Path("userId") userId:String
    ): Response<MessageResponse>
}