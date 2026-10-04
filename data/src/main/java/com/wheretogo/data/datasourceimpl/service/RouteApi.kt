package com.wheretogo.data.datasourceimpl.service

import com.wheretogo.data.model.route.RoutePathResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface RouteApi {
    @GET("api/route/{routeId}/path")
    suspend fun path(@Path("routeId") routeId: String): Response<RoutePathResponseDto>
}