package com.wheretogo.domain.repository

import com.wheretogo.domain.model.report.Report
import com.wheretogo.domain.model.report.ReportAddRequest

interface ReportRepository {
    suspend fun addReport(request: ReportAddRequest): Result<String>
    suspend fun getReport(reportId: String): Result<Report>
}