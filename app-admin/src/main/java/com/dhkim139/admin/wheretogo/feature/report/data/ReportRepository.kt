package com.dhkim139.admin.wheretogo.feature.report.data

import com.dhkim139.admin.wheretogo.feature.dashboard.DashboardStatsDto
import com.dhkim139.admin.wheretogo.feature.report.model.Report
import com.dhkim139.admin.wheretogo.feature.report.model.ReportInput
import com.dhkim139.admin.wheretogo.feature.report.model.ReportListResponse

interface ReportRepository {
    suspend fun getDashboardStats(): Result<DashboardStatsDto>
    suspend fun getReports(
        status: String? = null,
        moderate: String? = null,
        page: Int = 0,
        size: Int = 20,
    ): Result<ReportListResponse>
    suspend fun getReport(reportId: String): Result<Report>

    suspend fun approveReport(reportId: String, withUser: Boolean, input: ReportInput): Result<Unit>

    suspend fun rejectReport(reportId: String, withUser: Boolean, input: ReportInput): Result<Unit>
}
