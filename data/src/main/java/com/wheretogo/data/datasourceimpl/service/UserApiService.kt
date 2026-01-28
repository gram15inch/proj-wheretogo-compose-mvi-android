package com.wheretogo.data.datasourceimpl.service

import com.wheretogo.data.model.firebase.MessageResponse
import com.wheretogo.data.model.user.DataMsgToken
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApiService {
    @DELETE("api/user/{userId}")
    suspend fun deleteUser(
        @Path("userId") userId: String
    ): Response<MessageResponse>

    @POST("api/user/msgTokenUpdate")
    suspend fun updateMsgToken(
        @Body body: DataMsgToken
    ): Response<MessageResponse>
}