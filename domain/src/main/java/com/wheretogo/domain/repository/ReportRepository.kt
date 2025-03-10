package com.wheretogo.domain.repository

import com.wheretogo.domain.ReportStatus
import com.wheretogo.domain.ReportType
import com.wheretogo.domain.model.community.Report

interface ReportRepository {
    suspend fun addReport(report: Report): Result<Unit>
    suspend fun getReport(reportId: String): Result<Report>
    suspend fun removeReport(reportId: String): Result<Unit>
    suspend fun getReportByType(reportType: ReportType): Result<List<Report>>
    suspend fun getReportByStatus(reportStatus: ReportStatus): Result<List<Report>>
    suspend fun getReportByUid(userId: String): Result<List<Report>>
}