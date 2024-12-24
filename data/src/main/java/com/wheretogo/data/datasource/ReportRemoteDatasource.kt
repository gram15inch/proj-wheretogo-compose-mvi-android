package com.wheretogo.data.datasource

import com.wheretogo.data.model.report.RemoteReport

interface ReportRemoteDatasource {
    suspend fun addReport(report: RemoteReport): Boolean
    suspend fun getReport(reportID: String): RemoteReport?
    suspend fun getReportByType(reportType: String): List<RemoteReport>
    suspend fun getReportByStatus(reportStatus: String): List<RemoteReport>
}