package com.wheretogo.domain.repository

import com.wheretogo.domain.ReportStatus
import com.wheretogo.domain.ReportType
import com.wheretogo.domain.model.report.Report
import com.wheretogo.domain.model.report.ReportAddRequest

interface ReportRepository {
    suspend fun addReport(request: ReportAddRequest): Result<String>
    suspend fun getReport(reportId: String): Result<Report>
    suspend fun removeReport(reportId: String): Result<Unit>
    suspend fun getReportByType(reportType: ReportType): Result<List<Report>>
    suspend fun getReportByStatus(reportStatus: ReportStatus): Result<List<Report>>
    suspend fun getReportByUid(userId: String): Result<List<Report>>
}