package com.wheretogo.data.datasourceimpl.service

import com.wheretogo.data.model.course.CourseSyncResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CourseSyncApi {

    @GET("api/course/sync")
    suspend fun sync(
        @Query("cursor") cursor: Long,
        @Query("limit") limit: Int?,
    ): Response<CourseSyncResponseDto>
}
