package com.wheretogo.data.datasourceimpl.service

import com.wheretogo.data.model.course.CourseEditRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CourseManageApi {
    @PATCH("api/course/{id}")
    suspend fun edit(
        @Path("id") courseId: String,
        @Body body: CourseEditRequestDto,
    ): Response<Unit>

    @DELETE("api/course/{id}")
    suspend fun delete(@Path("id") courseId: String): Response<Unit>

    @POST("api/course/{id}/report")
    suspend fun report(@Path("id") courseId: String): Response<List<String>>
}
