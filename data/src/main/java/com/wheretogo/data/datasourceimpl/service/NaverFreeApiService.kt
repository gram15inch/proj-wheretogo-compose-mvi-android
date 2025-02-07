package com.wheretogo.data.datasourceimpl.service

import com.wheretogo.data.model.naver.free.NaverSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NaverFreeApiService {
    @GET("v1/search/local.json")
    suspend fun getAddressFromKeyword(
        @Header("X-Naver-Client-Id") clientId: String,
        @Header("X-Naver-Client-Secret") clientSecret: String,
        @Query("query") query: String,
        @Query("display") display: Int,
        @Query("start") start: Int,
        @Query("sort") sort: String,
    ): Response<NaverSearchResponse>
}