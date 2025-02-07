package com.wheretogo.data.datasourceimpl.service

import com.wheretogo.data.model.naver.NaverGeocodeResponse
import com.wheretogo.data.model.naver.NaverReverseGeocodeResponse
import com.wheretogo.data.model.naver.NaverRouteResponse
import com.wheretogo.data.model.naver.NaverRouteWaypointResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NaverMapApiService {
    @GET("map-direction/v1/driving")
    suspend fun getRoute(
        @Header("X-NCP-APIGW-API-KEY-ID") clientId: String,
        @Header("X-NCP-APIGW-API-KEY") clientSecret: String,
        @Query("start") start: String,
        @Query("goal") goal: String
    ): Response<NaverRouteResponse>

    @GET("map-direction/v1/driving")
    suspend fun getRouteWayPoint(
        @Header("X-NCP-APIGW-API-KEY-ID") clientId: String,
        @Header("X-NCP-APIGW-API-KEY") clientSecret: String,
        @Query("start") start: String,
        @Query("goal") goal: String,
        @Query("waypoints") waypoints: String,
    ): Response<NaverRouteWaypointResponse>

    @GET("map-geocode/v2/geocode")
    suspend fun geocode(
        @Header("X-NCP-APIGW-API-KEY-ID") clientId: String,
        @Header("X-NCP-APIGW-API-KEY") clientSecret: String,
        @Header("Accept") accept: String,
        @Query("query") query: String,
        @Query("count") count: String,
    ): Response<NaverGeocodeResponse>

    @GET("map-reversegeocode/v2/gc")
    suspend fun reverseGeocode(
        @Header("X-NCP-APIGW-API-KEY-ID") clientId: String,
        @Header("X-NCP-APIGW-API-KEY") clientSecret: String,
        @Query("coords") coords: String,
        @Query("output") output: String,
    ): Response<NaverReverseGeocodeResponse>

}