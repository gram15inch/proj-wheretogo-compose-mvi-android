package com.wheretogo.domain.repository

import com.wheretogo.domain.ReportStatus
import com.wheretogo.domain.ReportType
import com.wheretogo.domain.model.community.Report

interface ReportRepository {
    suspend fun addReport(report: Report)
    suspend fun getReport(reportID: String): Report?
    suspend fun getReportByType(reportType: ReportType): List<Report>
    suspend fun getReportByStatus(reportStatus: ReportStatus): List<Report>
}