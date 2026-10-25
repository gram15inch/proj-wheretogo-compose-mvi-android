package com.dhkim139.feature.navigationoption.data

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


internal interface NavOptionApi {

    @GET("/api/parking/course/{courseId}")
    suspend fun courseParking(
        @Path("courseId") courseId: String,
        @Query("direction") direction: String,
        @Query("limit") limit: Int?
    ): Response<CourseParkingOneWayDto> // 없으면 404
}
