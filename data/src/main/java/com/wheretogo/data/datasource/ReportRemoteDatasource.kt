package com.wheretogo.data.datasource

import com.wheretogo.data.model.report.ReportCreateContent
import com.wheretogo.data.model.report.ReportResponse

interface ReportRemoteDatasource {
    suspend fun addReport(content: ReportCreateContent): Result<String>
    suspend fun getReport(reportId: String): Result<ReportResponse>
}