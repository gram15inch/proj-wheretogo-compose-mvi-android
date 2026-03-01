package com.wheretogo.data.datasourceimpl.service

import com.wheretogo.data.model.firebase.DataResponse
import com.wheretogo.data.model.report.ReportRequest
import com.wheretogo.data.model.report.ReportResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReportApiService {
    @GET("api/report/{id}")
    suspend fun getReport(
        @Path("id") reportId: String
    ): Response<DataResponse<ReportResponse>>

    @POST("api/report/")
    suspend fun addReport(
        @Body request: ReportRequest
    ): Response<DataResponse<String>>
}