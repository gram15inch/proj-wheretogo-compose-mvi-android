package com.wheretogo.data.datasourceimpl.service

import com.wheretogo.data.model.user.UserDeleteResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Path

interface FirebaseApiService {
    @DELETE("api/user/{userId}")
    suspend fun deleteUser(
        @Path("userId") userId:String
    ): Response<UserDeleteResponse>
}