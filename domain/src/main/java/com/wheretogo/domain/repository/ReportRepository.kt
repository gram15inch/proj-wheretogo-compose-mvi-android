package com.wheretogo.domain.repository

import com.wheretogo.domain.ReportStatus
import com.wheretogo.domain.ReportType
import com.wheretogo.domain.model.community.Report

interface ReportRepository {
    suspend fun addReport(report: Report)
    suspend fun getReport(reportId: String): Report?
    suspend fun removeReport(reportId: String): Boolean
    suspend fun getReportByType(reportType: ReportType): List<Report>
    suspend fun getReportByStatus(reportStatus: ReportStatus): List<Report>
    suspend fun getReportByUid(userId: String): List<Report>
}