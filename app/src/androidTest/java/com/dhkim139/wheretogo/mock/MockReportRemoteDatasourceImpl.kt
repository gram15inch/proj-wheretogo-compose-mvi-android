package com.dhkim139.wheretogo.mock

import com.wheretogo.data.datasource.ReportRemoteDatasource
import com.wheretogo.data.model.report.RemoteReport
import javax.inject.Inject

class MockReportRemoteDatasourceImpl @Inject constructor() : ReportRemoteDatasource {
    private var newReportGroup = mutableListOf<RemoteReport>()
    override suspend fun addReport(report: RemoteReport): Boolean {
        newReportGroup.add(report)
        return true
    }

    override suspend fun getReport(reportId: String): RemoteReport? {
        return newReportGroup.firstOrNull { it.reportId == reportId }
    }

    override suspend fun removeReport(reportId: String): Boolean {
        return newReportGroup.removeIf { it.reportId == reportId }
    }

    override suspend fun getReportByType(reportType: String): List<RemoteReport> {
        return newReportGroup.filter { it.type == reportType }
    }

    override suspend fun getReportByStatus(reportStatus: String): List<RemoteReport> {
        return newReportGroup.filter { it.status == reportStatus }
    }

    override suspend fun getReportByUid(userId: String): List<RemoteReport> {
        return newReportGroup.filter { it.userId == userId }
    }
}