package com.wheretogo.data.datasource

import com.wheretogo.data.model.report.ReportRequest
import com.wheretogo.data.model.report.ReportResponse

interface ReportRemoteDatasource {
    suspend fun addReport(request: ReportRequest): Result<String>
    suspend fun getReport(reportId: String): Result<ReportResponse>
}