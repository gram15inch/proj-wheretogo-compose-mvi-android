package com.wheretogo.data.datasourceimpl.service

import com.wheretogo.data.model.auth.DataSyncToken
import com.wheretogo.data.model.content.ContentFilterRequest
import com.wheretogo.data.model.course.RemoteCourse
import com.wheretogo.data.model.firebase.DataResponse
import com.wheretogo.data.model.user.RemoteSyncUser
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface GuestApiService {

    @POST("api/guest/syncUser")
    suspend fun syncUser(
        @Body body: DataSyncToken,
    ): Response<DataResponse<RemoteSyncUser>>

    @POST("api/content/course")
    suspend fun getCourseByFilter(
        @Body filter: ContentFilterRequest,
    ): Response<DataResponse<List<RemoteCourse>>>

}