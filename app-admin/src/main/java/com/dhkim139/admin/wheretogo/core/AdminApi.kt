package com.dhkim139.admin.wheretogo.core

import com.dhkim139.admin.wheretogo.feature.dashboard.DashboardStatsDto
import com.dhkim139.admin.wheretogo.feature.report.model.ReportActionRequest
import com.dhkim139.admin.wheretogo.feature.report.model.ReportDto
import com.dhkim139.admin.wheretogo.feature.report.model.ReportListResponse
import com.wheretogo.data.model.firebase.DataResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminApi {

    @GET("api/admin/dashboard/stats")
    suspend fun getDashboardStats(): DataResponse<DashboardStatsDto>

    @GET("api/admin/reports")
    suspend fun getReports(
        @Query("status") status: String? = null,
        @Query("moderate") moderate: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): DataResponse<ReportListResponse>

    @GET("api/admin/report/{reportId}")
    suspend fun getReport(@Path("reportId") reportId: String): DataResponse<ReportDto>

    @PATCH("api/admin/report/{reportId}/action")
    suspend fun actionReport(
        @Path("reportId") reportId: String,
        @Body request: ReportActionRequest,
    )

}
