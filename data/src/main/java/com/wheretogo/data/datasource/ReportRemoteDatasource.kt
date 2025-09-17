package com.wheretogo.data.datasource

import com.wheretogo.data.model.report.RemoteReport

interface ReportRemoteDatasource {
    suspend fun addReport(report: RemoteReport): Result<Unit>
    suspend fun getReport(reportId: String): Result<RemoteReport>
    suspend fun removeReport(reportId: String): Result<Unit>
    suspend fun getReportByType(reportType: String): Result<List<RemoteReport>>
    suspend fun getReportByStatus(reportStatus: String): Result<List<RemoteReport>>
    suspend fun getReportByUid(userId: String): Result<List<RemoteReport>>
}