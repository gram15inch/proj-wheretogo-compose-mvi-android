package com.dhkim139.wheretogo.data.datasource.service

import com.dhkim139.wheretogo.data.model.naver.NaverRouteRespose
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NaverMapApiService {
    @GET("map-direction/v1/driving")
    suspend fun getRoute(
        @Header("X-NCP-APIGW-API-KEY-ID") clientId: String,
        @Header("X-NCP-APIGW-API-KEY") clientSecret: String,
        @Query("start") start:String,
        @Query("goal") goal:String
    ): Response<NaverRouteRespose>
}