package com.wheretogo.data.datasourceimpl.service

import com.wheretogo.data.model.user.UserDeleteRequest
import com.wheretogo.data.model.user.UserDeleteResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface FirebaseApiService {
        @POST("anonymizeAndDeleteUser")
    suspend fun deleteUser(
        @Body request: UserDeleteRequest,
        @Header("Authorization") authorization: String
    ): Response<UserDeleteResponse>
}