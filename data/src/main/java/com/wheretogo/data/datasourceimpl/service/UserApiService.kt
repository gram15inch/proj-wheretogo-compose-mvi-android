package com.wheretogo.data.datasourceimpl.service

import com.wheretogo.data.model.firebase.MessageResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Path

interface UserApiService {
    @DELETE("api/user/{userId}")
    suspend fun deleteUser(
        @Path("userId") userId: String
    ): Response<MessageResponse>
}