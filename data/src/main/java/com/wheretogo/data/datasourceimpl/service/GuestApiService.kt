package com.wheretogo.data.datasourceimpl.service

import com.wheretogo.data.model.firebase.DataResponse
import com.wheretogo.data.model.user.RemoteSyncUser
import com.wheretogo.domain.model.auth.SyncToken
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface GuestApiService {

    @POST("api/guest/syncUser")
    suspend fun syncUser(
        @Body body: SyncToken,
    ): Response<DataResponse<RemoteSyncUser>>
}